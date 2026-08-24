package net.yorunina.maa.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "dev.ftb.mods.ftbchunks.client.gui.WaypointEditorScreen$RowPanel")
public abstract class MixinWaypointEditorScreenRowPanel {
    @ModifyExpressionValue(method = "mousePressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasPermissions(I)Z"))
    private boolean showTeleportOption(boolean original) {
        return true;
    }
}
