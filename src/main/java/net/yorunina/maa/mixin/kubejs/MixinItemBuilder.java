package net.yorunina.maa.mixin.kubejs;

import dev.latvian.mods.kubejs.item.ItemBuilder;
import net.minecraft.resources.ResourceLocation;
import net.yorunina.maa.compat.kubejs.MAAItemBuilderExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds the item-builder API from KubeJS commit aede19af to MAA's runtime
 * compatibility layer.
 */
@Mixin(value = ItemBuilder.class, remap = false)
public abstract class MixinItemBuilder implements MAAItemBuilderExtension {
    @Unique
    public transient boolean canFitInsideContainerItems;

    @Unique
    @Nullable
    public transient TooltipImageCallback tooltipImage;

    @Unique
    @Nullable
    public transient OverrideStackedOnOtherCallback overrideStackedOnOther;

    @Unique
    @Nullable
    public transient OverrideOtherStackedOnMeCallback overrideOtherStackedOnMe;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void maa$init(ResourceLocation id, CallbackInfo ci) {
        canFitInsideContainerItems = true;
    }

    @Override
    public boolean maa$canFitInsideContainerItems() {
        return canFitInsideContainerItems;
    }

    @Override
    public void maa$setCanFitInsideContainerItems(boolean value) {
        canFitInsideContainerItems = value;
    }

    @Override
    @Nullable
    public TooltipImageCallback maa$getTooltipImage() {
        return tooltipImage;
    }

    @Override
    public void maa$setTooltipImage(@Nullable TooltipImageCallback callback) {
        tooltipImage = callback;
    }

    @Override
    @Nullable
    public OverrideStackedOnOtherCallback maa$getOverrideStackedOnOther() {
        return overrideStackedOnOther;
    }

    @Override
    public void maa$setOverrideStackedOnOther(@Nullable OverrideStackedOnOtherCallback callback) {
        overrideStackedOnOther = callback;
    }

    @Override
    @Nullable
    public OverrideOtherStackedOnMeCallback maa$getOverrideOtherStackedOnMe() {
        return overrideOtherStackedOnMe;
    }

    @Override
    public void maa$setOverrideOtherStackedOnMe(@Nullable OverrideOtherStackedOnMeCallback callback) {
        overrideOtherStackedOnMe = callback;
    }

    @Unique
    public ItemBuilder canFitInsideContainerItems(boolean value) {
        maa$setCanFitInsideContainerItems(value);
        return (ItemBuilder) (Object) this;
    }

    @Unique
    public ItemBuilder tooltipImage(TooltipImageCallback callback) {
        maa$setTooltipImage(callback);
        return (ItemBuilder) (Object) this;
    }

    @Unique
    public ItemBuilder overrideStackedOnOther(OverrideStackedOnOtherCallback callback) {
        maa$setOverrideStackedOnOther(callback);
        return (ItemBuilder) (Object) this;
    }

    @Unique
    public ItemBuilder overrideOtherStackedOnMe(OverrideOtherStackedOnMeCallback callback) {
        maa$setOverrideOtherStackedOnMe(callback);
        return (ItemBuilder) (Object) this;
    }
}
