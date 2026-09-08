package net.yorunina.maa.compat.leaderboards;

import com.leclowndu93150.leaderboards.data.Leaderboard;
import com.leclowndu93150.leaderboards.util.OfflinePlayerStats;

import java.util.function.Function;

public interface ILeaderboard {
    int getIntValue(OfflinePlayerStats player);

    void setIntValueFunction(Function<OfflinePlayerStats, Integer> function);

    static int score(Leaderboard leaderboard, OfflinePlayerStats player) {
        return ((ILeaderboard) leaderboard).getIntValue(player);
    }

    static void setScoreFunction(Leaderboard leaderboard, Function<OfflinePlayerStats, Integer> function) {
        if (leaderboard != null) {
            ((ILeaderboard) leaderboard).setIntValueFunction(function);
        }
    }
}
