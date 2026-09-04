package net.yorunina.maa.mixin;

import dev.latvian.mods.kubejs.core.ItemKJS;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yorunina.maa.compat.kubejs.MAAItemBuilderExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Applies the item behavior hooks introduced by KubeJS commit aede19af.
 * KubeJS' ItemMixin remains responsible for storing the original builder; MAA
 * reads it through the existing ItemKJS bridge.
 */
@Mixin(value = Item.class)
public abstract class MixinItem {
    @Unique
    @Nullable
    private MAAItemBuilderExtension maa$getItemBuilderExtension() {
        ItemBuilder builder = ((ItemKJS) this).kjs$getItemBuilder();
        return builder instanceof MAAItemBuilderExtension extension ? extension : null;
    }

    @Inject(method = "canFitInsideContainerItems", at = @At("HEAD"), cancellable = true)
    private void maa$canFitInsideContainerItems(CallbackInfoReturnable<Boolean> cir) {
        MAAItemBuilderExtension extension = maa$getItemBuilderExtension();
        if (extension != null && !extension.maa$canFitInsideContainerItems()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
    private void maa$getTooltipImage(ItemStack stack, CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        MAAItemBuilderExtension extension = maa$getItemBuilderExtension();
        if (extension != null && extension.maa$getTooltipImage() != null) {
            cir.setReturnValue(extension.maa$getTooltipImage().getTooltipImage(stack));
        }
    }

    @Inject(method = "overrideStackedOnOther", at = @At("HEAD"), cancellable = true)
    private void maa$overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player, CallbackInfoReturnable<Boolean> cir) {
        MAAItemBuilderExtension extension = maa$getItemBuilderExtension();
        if (extension != null && extension.maa$getOverrideStackedOnOther() != null) {
            cir.setReturnValue(extension.maa$getOverrideStackedOnOther().overrideStackedOnOther(stack, slot, action, player));
        }
    }

    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    private void maa$overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
        MAAItemBuilderExtension extension = maa$getItemBuilderExtension();
        if (extension != null && extension.maa$getOverrideOtherStackedOnMe() != null) {
            cir.setReturnValue(extension.maa$getOverrideOtherStackedOnMe().overrideOtherStackedOnMe(stack, other, slot, action, player, access));
        }
    }
}
