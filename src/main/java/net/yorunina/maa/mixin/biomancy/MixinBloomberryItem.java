package net.yorunina.maa.mixin.biomancy;

import com.github.elenterius.biomancy.item.BloomberryItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BloomberryItem.class)
public abstract class MixinBloomberryItem {
    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    private void maa$skipRandomPotion(ItemStack stack, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(livingEntity.eat(level, stack));
    }
}