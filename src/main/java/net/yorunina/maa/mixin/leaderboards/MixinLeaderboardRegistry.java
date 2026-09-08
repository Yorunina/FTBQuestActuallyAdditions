package net.yorunina.maa.mixin.leaderboards;

import com.leclowndu93150.leaderboards.LeaderboardRegistry;
import net.yorunina.maa.compat.kubejs.events.LeaderboardRegistryEventJS;
import net.yorunina.maa.compat.leaderboards.LeaderboardsCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.yorunina.maa.compat.kubejs.MAAEvents.REGISTRY_LEADERBOARDS_EVENT;

@Mixin(value = LeaderboardRegistry.class, remap = false)
public abstract class MixinLeaderboardRegistry {
    @Inject(method = "register", at = @At("RETURN"))
    private static void maa$afterRegister(CallbackInfo ci) {
        LeaderboardsCompat.attach();
        REGISTRY_LEADERBOARDS_EVENT.post(new LeaderboardRegistryEventJS());
    }
}
