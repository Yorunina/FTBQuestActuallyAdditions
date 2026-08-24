package net.yorunina.maa.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ftb.mods.ftbchunks.client.mapicon.WaypointMapIcon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WaypointMapIcon.class)
public abstract class MixinWaypointMapIcon {
    @ModifyExpressionValue(method = "openWPContextMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasPermissions(I)Z"))
    private boolean showTeleportOption(boolean original) {
        return true;
    }
}
