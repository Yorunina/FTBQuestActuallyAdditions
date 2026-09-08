package net.yorunina.maa.compat.leaderboards;

import com.leclowndu93150.leaderboards.data.Leaderboard;
import com.leclowndu93150.leaderboards.util.KnownPlayers;
import com.leclowndu93150.leaderboards.util.OfflinePlayerStats;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerAchieveStatTask extends LeaderboardTask {
    private long value = 1;

    private static final Map<ResourceLocation, Long> CACHED_TOTALS = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, Long> LAST_CALC_TIME = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 30 * 1000;
    private static final Object CALCULATION_LOCK = new Object();

    public ServerAchieveStatTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public TaskType getType() {
        return LeaderboardsCompat.SERVER_ACHIEVE_STAT_TASK;
    }

    @Override
    public long getMaxProgress() {
        return this.value;
    }

    @Override
    public String formatMaxProgress() {
        return Long.toString(this.value);
    }

    @Override
    public String formatProgress(TeamData teamData, long progress) {
        return Long.toUnsignedString(progress);
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putLong("value", this.value);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        this.value = nbt.getLong("value");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeLong(this.value);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.value = buffer.readLong();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addLong("value", this.value, v -> this.value = v, 1, -2147483647, 2147483647);
    }

    @Override
    public void submitTask(TeamData teamData, ServerPlayer player, ItemStack craftedItem) {
        if (teamData.isCompleted(this) || !checkTaskSequence(teamData)) return;

        Leaderboard leaderBoard = resolveLeaderboard();
        if (leaderBoard == null) {
            return;
        }
        teamData.setProgress(this, getServerTotalFromCache(player.getServer(), leaderBoard));
    }

    public static long getServerTotalFromCache(MinecraftServer server, Leaderboard leaderboard) {
        long now = System.currentTimeMillis();
        Long lastCalc = LAST_CALC_TIME.get(leaderboard.id);
        Long cached = CACHED_TOTALS.get(leaderboard.id);
        if (lastCalc != null && cached != null && now - lastCalc < CACHE_DURATION) {
            return cached;
        }

        synchronized (CALCULATION_LOCK) {
            lastCalc = LAST_CALC_TIME.get(leaderboard.id);
            cached = CACHED_TOTALS.get(leaderboard.id);
            if (lastCalc != null && cached != null && now - lastCalc < CACHE_DURATION) {
                return cached;
            }
            refreshServerTotalCache(server, leaderboard);
        }
        return CACHED_TOTALS.getOrDefault(leaderboard.id, 0L);
    }

    public static void refreshServerTotalCache(MinecraftServer server, Leaderboard leaderboard) {
        long total = 0;
        for (UUID uuid : KnownPlayers.all(server)) {
            total += OfflinePlayerStats.of(server, uuid)
                    .map(stats -> ILeaderboard.score(leaderboard, stats))
                    .orElse(0);
        }
        CACHED_TOTALS.put(leaderboard.id, total);
        LAST_CALC_TIME.put(leaderboard.id, System.currentTimeMillis());
    }
}
