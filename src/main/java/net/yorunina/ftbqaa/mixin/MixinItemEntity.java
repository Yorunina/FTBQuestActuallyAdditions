package net.yorunina.ftbqaa.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.yorunina.ftbqaa.FTBQAATags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ItemEntity.class, priority = 1004)
public abstract class MixinItemEntity {
    @Shadow public abstract ItemStack getItem();

    @WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;isInvulnerableTo(Lnet/minecraft/world/damagesource/DamageSource;)Z"))
    private boolean applyTagsImmuneDamages(ItemEntity instance, DamageSource damageSource, Operation<Boolean> original) {
        return original.call(instance, damageSource) || !this.getItem().isEmpty() &&
                ((this.getItem().is(FTBQAATags.IMMUNE_CACTUS) && damageSource.is(DamageTypes.CACTUS)) ||
                        (this.getItem().is(FTBQAATags.IMMUNE_EXPLOSION) && damageSource.is(DamageTypeTags.IS_EXPLOSION)) ||
                        (this.getItem().is(FTBQAATags.IMMUNE_LIGHTNING) && damageSource.is(DamageTypeTags.IS_LIGHTNING)));
    }

    @ModifyReturnValue(method = "fireImmune", at = @At(value = "RETURN"))
    private boolean applyTagImmuneFire(boolean original) {
        return original || this.getItem().is(FTBQAATags.IMMUNE_FIRE);
    }
}