package net.yorunina.maa.mixin;

import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.unusual.block_factorys_bosses.init.BossesRiseKeyMappings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossesRiseKeyMappings.class)
public abstract class MixinBossesRiseKeyMappings {
    @Inject(method = "registerKeyMappings", at = @At("HEAD"), cancellable = true, remap = false)
    private static void maa$skipDodgeRollKey(RegisterKeyMappingsEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}