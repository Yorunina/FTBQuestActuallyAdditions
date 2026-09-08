package net.yorunina.maa.compat.kubejs.events;

import com.leclowndu93150.leaderboards.LeaderboardRegistry;
import com.leclowndu93150.leaderboards.data.Leaderboard;
import com.leclowndu93150.leaderboards.util.OfflinePlayerStats;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.yorunina.maa.compat.leaderboards.ILeaderboard;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;

public class LeaderboardRegistryEventJS extends EventJS {
    public LeaderboardRegistryEventJS() {
        super();
    }

    public void register(ResourceLocation id, Component title, Function<OfflinePlayerStats, Component> valueFunction, Function<OfflinePlayerStats, Double> intValueFunction, Comparator<OfflinePlayerStats> comparator, Predicate<OfflinePlayerStats> validValue) {
        Leaderboard leaderboard = new Leaderboard(id, title, valueFunction, comparator, validValue);
        ILeaderboard.setScoreFunction(leaderboard, v -> intValueFunction.apply(v).intValue());
        LeaderboardRegistry.LEADERBOARDS.put(id, leaderboard);
    }

    public void registerByLeaderboard(ResourceLocation id, Leaderboard leaderboard) {
        LeaderboardRegistry.LEADERBOARDS.put(id, leaderboard);
    }
}
