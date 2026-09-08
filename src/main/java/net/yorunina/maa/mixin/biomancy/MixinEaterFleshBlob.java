package net.yorunina.maa.mixin.biomancy;

import com.github.elenterius.biomancy.entity.mob.fleshblob.EaterFleshBlob;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.ItemStack;
import net.yorunina.maa.compat.biomancy.BiomancyOrganHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EaterFleshBlob.class)
public abstract class MixinEaterFleshBlob {
    @ModifyReturnValue(method = "canHoldItem", at = @At("RETURN"))
    private boolean maa$canHoldOrganItem(boolean original, ItemStack stack) {
        return original || BiomancyOrganHelper.canHoldOrganItem(stack);
    }
}