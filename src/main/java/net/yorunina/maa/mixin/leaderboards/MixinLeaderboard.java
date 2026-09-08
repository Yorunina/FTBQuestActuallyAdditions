package net.yorunina.maa.mixin.leaderboards;

import com.leclowndu93150.leaderboards.data.Leaderboard;
import com.leclowndu93150.leaderboards.util.OfflinePlayerStats;
import net.yorunina.maa.compat.leaderboards.ILeaderboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Function;

@Mixin(value = Leaderboard.class, remap = false)
public abstract class MixinLeaderboard implements ILeaderboard {
    @Unique
    private Function<OfflinePlayerStats, Integer> maa$intValueFunction = player -> 0;

    @Override
    public int getIntValue(OfflinePlayerStats player) {
        return this.maa$intValueFunction.apply(player);
    }

    @Override
    public void setIntValueFunction(Function<OfflinePlayerStats, Integer> function) {
        this.maa$intValueFunction = function == null ? player -> 0 : function;
    }
}
