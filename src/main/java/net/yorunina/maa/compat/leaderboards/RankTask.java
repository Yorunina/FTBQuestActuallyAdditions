package net.yorunina.maa.compat.leaderboards;

import com.leclowndu93150.leaderboards.data.Leaderboard;
import com.leclowndu93150.leaderboards.util.KnownPlayers;
import com.leclowndu93150.leaderboards.util.OfflinePlayerStats;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RankTask extends LeaderboardTask {
    private int min = 0;
    private int rank = 1;

    private record PlayerScore(UUID uuid, int score) {
    }

    private static final Map<ResourceLocation, List<PlayerScore>> RANKING_CACHE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, Long> LAST_RANK_CALC_TIME = new ConcurrentHashMap<>();
    private static final long RANK_CACHE_DURATION = 30 * 1000;
    private static final Object RANK_CALCULATION_LOCK = new Object();

    public RankTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public TaskType getType() {
        return LeaderboardsCompat.RANK_TASK;
    }

    @Override
    public long getMaxProgress() {
        return 1;
    }

    @Override
    public String formatMaxProgress() {
        return Integer.toString(1);
    }

    @Override
    public String formatProgress(TeamData teamData, long progress) {
        return Long.toUnsignedString(progress);
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putInt("min", this.min);
        nbt.putInt("rank", this.rank);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        this.min = nbt.getInt("min");
        this.rank = nbt.getInt("rank");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeVarInt(this.min);
        buffer.writeVarInt(this.rank);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.min = buffer.readVarInt();
        this.rank = buffer.readVarInt();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addInt("min", this.min, v -> this.min = v, 0, -2147483647, 2147483647);
        config.addInt("rank", this.rank, v -> this.rank = v, 1, -2147483647, 2147483647);
    }

    @Override
    public void submitTask(TeamData teamData, ServerPlayer player, ItemStack craftedItem) {
        if (teamData.isCompleted(this) || !checkTaskSequence(teamData)) return;

        Leaderboard leaderBoard = resolveLeaderboard();
        if (leaderBoard == null) return;
        if (meetsRank(player, leaderBoard)) {
            teamData.addProgress(this, 1);
        }
    }

    private boolean meetsRank(ServerPlayer player, Leaderboard leaderboard) {
        if (this.rank == 0) {
            return false;
        }

        int targetPlayerScore = ILeaderboard.score(leaderboard, OfflinePlayerStats.of(player));
        if (this.min > 0 && targetPlayerScore < this.min) {
            return false;
        }

        boolean ascending = this.rank < 0;
        List<PlayerScore> sortedScores = sortedScores(player.getServer(), leaderboard, ascending);
        if (sortedScores.isEmpty()) {
            return false;
        }

        int effectiveTargetRank = Math.abs(this.rank);
        int playerRank = -1;
        int rankCounter = 1;
        int playersAtCurrentRank = 1;
        for (int i = 0; i < sortedScores.size(); i++) {
            PlayerScore current = sortedScores.get(i);
            if (i > 0 && current.score() != sortedScores.get(i - 1).score()) {
                rankCounter += playersAtCurrentRank;
                playersAtCurrentRank = 1;
            } else if (i > 0) {
                playersAtCurrentRank++;
            }
            if (current.uuid().equals(player.getUUID())) {
                playerRank = rankCounter;
                break;
            }
        }

        return playerRank != -1 && playerRank <= effectiveTargetRank;
    }

    private List<PlayerScore> sortedScores(MinecraftServer server, Leaderboard leaderboard, boolean ascending) {
        long now = System.currentTimeMillis();
        if (now - LAST_RANK_CALC_TIME.getOrDefault(leaderboard.id, 0L) < RANK_CACHE_DURATION && RANKING_CACHE.containsKey(leaderboard.id)) {
            return RANKING_CACHE.getOrDefault(leaderboard.id, List.of());
        }

        synchronized (RANK_CALCULATION_LOCK) {
            if (now - LAST_RANK_CALC_TIME.getOrDefault(leaderboard.id, 0L) < RANK_CACHE_DURATION && RANKING_CACHE.containsKey(leaderboard.id)) {
                return RANKING_CACHE.getOrDefault(leaderboard.id, List.of());
            }

            List<PlayerScore> scores = new ArrayList<>();
            for (UUID uuid : KnownPlayers.all(server)) {
                OfflinePlayerStats.of(server, uuid).ifPresent(stats ->
                        scores.add(new PlayerScore(uuid, ILeaderboard.score(leaderboard, stats))));
            }

            Comparator<PlayerScore> comparator = Comparator.comparingInt(PlayerScore::score);
            scores.sort(ascending ? comparator : comparator.reversed());

            RANKING_CACHE.put(leaderboard.id, scores);
            LAST_RANK_CALC_TIME.put(leaderboard.id, now);
            return scores;
        }
    }
}
