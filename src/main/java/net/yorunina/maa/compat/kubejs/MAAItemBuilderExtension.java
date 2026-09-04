package net.yorunina.maa.compat.kubejs;

import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * MAA-owned state used to extend KubeJS' ItemBuilder without changing KubeJS
 * sources.
 */
public interface MAAItemBuilderExtension {
    boolean maa$canFitInsideContainerItems();

    void maa$setCanFitInsideContainerItems(boolean value);

    @Nullable
    TooltipImageCallback maa$getTooltipImage();

    void maa$setTooltipImage(@Nullable TooltipImageCallback callback);

    @Nullable
    OverrideStackedOnOtherCallback maa$getOverrideStackedOnOther();

    void maa$setOverrideStackedOnOther(@Nullable OverrideStackedOnOtherCallback callback);

    @Nullable
    OverrideOtherStackedOnMeCallback maa$getOverrideOtherStackedOnMe();

    void maa$setOverrideOtherStackedOnMe(@Nullable OverrideOtherStackedOnMeCallback callback);

    @FunctionalInterface
    interface TooltipImageCallback {
        Optional<TooltipComponent> getTooltipImage(ItemStack stack);
    }

    @FunctionalInterface
    interface OverrideStackedOnOtherCallback {
        boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player);
    }

    @FunctionalInterface
    interface OverrideOtherStackedOnMeCallback {
        boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access);
    }
}
