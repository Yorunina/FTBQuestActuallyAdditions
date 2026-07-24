package net.yorunina.maa.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.dummmmmmy.network.ClientBoundDamageNumberMessage;
import net.minecraft.world.entity.Entity;
import net.yorunina.maa.model.ILivingEntityNumberPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientBoundDamageNumberMessage.class)
public class MixinClientBoundDamageNumberMessage {
    @ModifyArg(method = "spawnParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), index = 2)
    private double modifySpawnParticle(double p_46632_) {
        return p_46632_ + 0.5;
    }

    @ModifyArg(method = "handle", at = @At(value = "INVOKE", target = "Lnet/mehvahdjukaar/dummmmmmy/network/ClientBoundDamageNumberMessage;spawnParticle(Lnet/minecraft/world/entity/Entity;I)V", ordinal = 1), index = 1, remap = false)
    private int modifyHandle(int animationPos, @Local(name = "entity") Entity entity) {
        if (entity instanceof ILivingEntityNumberPos livingEntity) {
            return livingEntity.getNextNumberPos();
        }
        return animationPos;
    }
}
