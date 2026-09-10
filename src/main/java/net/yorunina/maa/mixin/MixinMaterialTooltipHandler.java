package net.yorunina.maa.mixin;

import io.github.createdelight.tetrainsight.client.MaterialTooltipHandler;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MaterialTooltipHandler.class)
public class MixinMaterialTooltipHandler {
    @Inject(method = "onTooltip", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onTooltip(ItemTooltipEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}
