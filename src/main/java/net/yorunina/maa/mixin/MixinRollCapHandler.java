package net.yorunina.maa.mixin;

import net.minecraft.world.entity.player.Player;
import net.unusual.block_factorys_bosses.capability.entity.RollCap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RollCap.RollCapHandler.class)
public abstract class MixinRollCapHandler {
    @Inject(method = "startRoll", at = @At("HEAD"), cancellable = true, remap = false)
    private void maa$neverRoll(Player player, float leftImpulse, float forwardImpulse, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}