package net.yorunina.maa.compat.leaderboards;

import com.leclowndu93150.leaderboards.LeaderboardRegistry;
import com.leclowndu93150.leaderboards.Leaderboards;
import com.leclowndu93150.leaderboards.VanillaStatsRegistry;
import com.leclowndu93150.leaderboards.data.Leaderboard;
import com.leclowndu93150.leaderboards.data.PlayerDataTracker;
import com.leclowndu93150.leaderboards.util.OfflinePlayerStats;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraftforge.fml.ModList;

import java.util.Comparator;

public final class LeaderboardsCompat {
    public static final TaskType RANK_TASK = TaskTypes.register(
            id("rank_task"),
            RankTask::new,
            () -> Icon.getIcon(ResourceLocation.fromNamespaceAndPath("minecraft", "item/iron_sword"))
    );
    public static final TaskType SERVER_ACHIEVE_STAT_TASK = TaskTypes.register(
            id("server_achieve_stat"),
            ServerAchieveStatTask::new,
            () -> Icon.getIcon(ResourceLocation.fromNamespaceAndPath("minecraft", "item/diamond_sword"))
    );

    private LeaderboardsCompat() {
    }

    public static void init() {
    }

    public static Leaderboard resolve(ResourceLocation id) {
        Leaderboard leaderboard = LeaderboardRegistry.LEADERBOARDS.get(id);
        return leaderboard != null ? leaderboard : VanillaStatsRegistry.VANILLA_STATS.get(id);
    }

    public static void attach() {
        ILeaderboard.setScoreFunction(LeaderboardRegistry.LEADERBOARDS.get(id("deaths_per_hour")),
                player -> (int) (deathsPerHour(player) * 100));
        ILeaderboard.setScoreFunction(LeaderboardRegistry.LEADERBOARDS.get(id("last_seen")), player -> {
            if (player.isOnline()) {
                return 0;
            }
            return (int) (player.server().overworld().getGameTime() - PlayerDataTracker.get(player.server()).getLastSeen(player.uuid()));
        });

        ILeaderboard.setScoreFunction(VanillaStatsRegistry.VANILLA_STATS.get(id("total_blocks_mined")), player -> sum(Stats.BLOCK_MINED, player));
        ILeaderboard.setScoreFunction(VanillaStatsRegistry.VANILLA_STATS.get(id("total_items_crafted")), player -> sum(Stats.ITEM_CRAFTED, player));
        ILeaderboard.setScoreFunction(VanillaStatsRegistry.VANILLA_STATS.get(id("total_items_used")), player -> sum(Stats.ITEM_USED, player));
        ILeaderboard.setScoreFunction(VanillaStatsRegistry.VANILLA_STATS.get(id("total_items_broken")), player -> sum(Stats.ITEM_BROKEN, player));
        ILeaderboard.setScoreFunction(VanillaStatsRegistry.VANILLA_STATS.get(id("total_items_picked_up")), player -> sum(Stats.ITEM_PICKED_UP, player));

        if (ModList.get().isLoaded("ftbquests")) {
            attachQuestLeaderboards();
        }
    }

    private static void attachQuestLeaderboards() {
        ResourceLocation completions = id("quest_completions");
        LeaderboardRegistry.LEADERBOARDS.computeIfAbsent(completions, key -> new Leaderboard(
                key,
                Component.translatable("leaderboard.leaderboards.quest_completions"),
                player -> Component.literal(String.valueOf(questCompletions(player))),
                Comparator.comparingInt(LeaderboardsCompat::questCompletions).reversed(),
                player -> questCompletions(player) > 0
        ));
        ILeaderboard.setScoreFunction(LeaderboardRegistry.LEADERBOARDS.get(completions), LeaderboardsCompat::questCompletions);

        ResourceLocation percentage = id("quest_completion_percentage");
        LeaderboardRegistry.LEADERBOARDS.computeIfAbsent(percentage, key -> new Leaderboard(
                key,
                Component.translatable("leaderboard.leaderboards.quest_completion_percentage"),
                player -> Component.literal(String.format("%.1f%%", questCompletionPercentage(player))),
                Comparator.comparingDouble(LeaderboardsCompat::questCompletionPercentage).reversed(),
                player -> questCompletions(player) > 0
        ));
        ILeaderboard.setScoreFunction(LeaderboardRegistry.LEADERBOARDS.get(percentage),
                player -> (int) (questCompletionPercentage(player) * 100));
    }

    private static int questCompletions(OfflinePlayerStats player) {
        if (!player.isOnline()) {
            return 0;
        }
        ServerPlayer onlinePlayer = player.onlinePlayer();
        if (onlinePlayer == null) {
            return 0;
        }
        try {
            ServerQuestFile questFile = (ServerQuestFile) FTBQuestsAPI.api().getQuestFile(false);
            if (questFile == null) {
                return 0;
            }
            TeamData teamData = questFile.getOrCreateTeamData(onlinePlayer);
            if (teamData == null) {
                return 0;
            }
            int[] completed = {0};
            questFile.forAllQuests(quest -> {
                if (teamData.isCompleted(quest)) {
                    completed[0]++;
                }
            });
            return completed[0];
        } catch (Exception e) {
            return 0;
        }
    }

    private static double questCompletionPercentage(OfflinePlayerStats player) {
        int completed = questCompletions(player);
        int total = 0;
        try {
            ServerQuestFile questFile = (ServerQuestFile) FTBQuestsAPI.api().getQuestFile(false);
            if (questFile != null) {
                int[] count = {0};
                questFile.forAllQuests(quest -> count[0]++);
                total = count[0];
            }
        } catch (Exception ignored) {
        }
        return total == 0 ? 0.0 : (double) completed / total * 100.0;
    }

    private static int sum(Iterable<? extends Stat<?>> stats, OfflinePlayerStats player) {
        int total = 0;
        for (Stat<?> stat : stats) {
            total += player.getValue(stat);
        }
        return total;
    }

    private static double deathsPerHour(OfflinePlayerStats player) {
        int playTime = player.getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
        if (playTime > 0) {
            double hours = playTime / 72000D;
            if (hours >= 1D) {
                return (double) player.getValue(Stats.CUSTOM.get(Stats.DEATHS)) / hours;
            }
        }
        return -1D;
    }

    static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Leaderboards.MODID, path);
    }
}
