package net.yorunina.maa.mixin.biomancy;

import com.github.elenterius.biomancy.entity.mob.FoodEater;
import com.github.elenterius.biomancy.entity.mob.ai.goal.EatFoodItemGoal;
import com.github.elenterius.biomancy.entity.mob.fleshblob.EaterFleshBlob;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.item.ItemStack;
import net.yorunina.maa.compat.biomancy.BiomancyOrganHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EatFoodItemGoal.class)
public abstract class MixinEatFoodItemGoal {
    @Unique
    private FoodEater maa$eater;

    @WrapOperation(
            method = "hasEdibleFood",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/elenterius/biomancy/entity/mob/FoodEater;getFoodItem()Lnet/minecraft/world/item/ItemStack;",
                    remap = false
            ),
            remap = false
    )
    private ItemStack maa$captureEater(FoodEater eater, Operation<ItemStack> original) {
        this.maa$eater = eater;
        return original.call(eater);
    }

    @ModifyReturnValue(method = "hasEdibleFood", at = @At("RETURN"), remap = false)
    private boolean maa$allowOrganFood(boolean original) {
        return original || (this.maa$eater != null && BiomancyOrganHelper.canHoldOrganItem(this.maa$eater.getFoodItem()));
    }

    @WrapOperation(method = "start", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration()I"))
    private int maa$organEatDuration(ItemStack stack, Operation<Integer> original) {
        return stack.getItem().isEdible() ? original.call(stack) : 300;
    }

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/elenterius/biomancy/entity/mob/ai/goal/EatFoodItemGoal;hasEdibleFood()Z",
                    remap = false
            )
    )
    private boolean maa$spitIncompatibleOrgan(EatFoodItemGoal<?> instance, Operation<Boolean> original) {
        if (this.maa$eater instanceof EaterFleshBlob blob) {
            ItemStack stack = blob.getFoodItem();
            if (BiomancyOrganHelper.canHoldOrganItem(stack)) {
                stack.removeTagKey(BiomancyOrganHelper.ORGAN_COMPATIBILITY_TAG);
                blob.spawnAtLocation(stack);
                blob.setFoodItem(ItemStack.EMPTY);
                blob.hurt(blob.level().damageSources().magic(), 5f);
            }
        }
        return original.call(instance);
    }
}