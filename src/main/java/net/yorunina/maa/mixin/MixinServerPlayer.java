package net.yorunina.maa.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import net.yorunina.maa.model.IFoodData;
import net.yorunina.maa.model.IPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player {
    @Shadow
    public abstract void stopRiding();

    public MixinServerPlayer(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "disconnect", at = @At("HEAD"))
    private void disconnectInject(CallbackInfo ci) {
        if (this.isPassenger()) {
            if (this.getVehicle() instanceof Player)
                this.stopRiding();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickInjectRemovePassengerOnInterruption(CallbackInfo callbackInfo) {
        if (this.isVehicle() && !this.isCrouching() && !this.isFallFlying() && this.fallDistance > 2F)
            if (this.getFirstPassenger() != null)
                this.getFirstPassenger().stopRiding();
    }


    @ModifyExpressionValue(method = "restoreFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean restoreFromModifyExpressionValue(boolean bool, @Local(name = "p_9016_") ServerPlayer serverPlayer) {
        return bool || ((IPlayer) serverPlayer).shouldKeepInventory();
    }

    @Inject(method = "restoreFrom", at = @At(value = "HEAD"))
    private void restoreFromInject(ServerPlayer p_9016_, boolean p_9017_, CallbackInfo ci) {
        IFoodData foodData = (IFoodData) this.getFoodData();
        ((IFoodData) this.foodData).setNoAddExhaustion(foodData.getNoAddExhaustion());
        IPlayer iPlayer = (IPlayer) p_9016_;
        ((IPlayer) this).setKeepInventory(iPlayer.shouldKeepInventory());
        ((IPlayer) this).setMapTeleportBypass(iPlayer.canMapTeleport());
    }
}