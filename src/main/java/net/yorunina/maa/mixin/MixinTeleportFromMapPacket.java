package net.yorunina.maa.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ftb.mods.ftbchunks.net.TeleportFromMapPacket;
import net.minecraft.server.level.ServerPlayer;
import net.yorunina.maa.model.IPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TeleportFromMapPacket.class)
public abstract class MixinTeleportFromMapPacket {
    @ModifyExpressionValue(method = "handle", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;hasPermissions(I)Z"))
    private boolean modifyTeleportPermission(boolean original, @Local ServerPlayer player) {
        return original || ((IPlayer) player).canMapTeleport();
    }
}
