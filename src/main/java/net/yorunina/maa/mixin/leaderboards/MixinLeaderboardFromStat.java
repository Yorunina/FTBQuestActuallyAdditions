package net.yorunina.maa.mixin.leaderboards;

import com.leclowndu93150.leaderboards.data.Leaderboard;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.yorunina.maa.compat.leaderboards.ILeaderboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.IntFunction;

@Mixin(value = Leaderboard.FromStat.class, remap = false)
public abstract class MixinLeaderboardFromStat {
    @Inject(
            method = "<init>(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/chat/Component;Lnet/minecraft/stats/Stat;ZLjava/util/function/IntFunction;)V",
            at = @At("RETURN")
    )
    private void maa$captureStat(ResourceLocation id, Component title, Stat<?> stat, boolean ascending, IntFunction<Component> valueFormatter, CallbackInfo ci) {
        ((ILeaderboard) this).setIntValueFunction(player -> player.getValue(stat));
    }
}
