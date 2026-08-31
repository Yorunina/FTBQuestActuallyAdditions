package net.yorunina.maa.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.player.Player;
import net.yorunina.maa.model.IPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class MixinPlayer implements IPlayer {
    @Unique
    private boolean keepInventory = false;

    @Unique
    private boolean mapTeleportBypass = false;

    @Unique
    private boolean alwaysFly = false;

    @Unique
    public boolean shouldKeepInventory() {
        return keepInventory;
    }

    @Unique
    public void setKeepInventory(boolean keepInventory) {
        this.keepInventory = keepInventory;
    }

    @Unique
    public boolean canMapTeleport() {
        return mapTeleportBypass;
    }

    @Unique
    public void setMapTeleportBypass(boolean bypass) {
        this.mapTeleportBypass = bypass;
    }

    @ModifyExpressionValue(method = "dropEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean dropEquipment(boolean bool) {
        return bool || this.keepInventory;
    }

    @ModifyExpressionValue(method = "getExperienceReward", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean getExperienceReward(boolean bool) {
        return bool || this.keepInventory;
    }
}