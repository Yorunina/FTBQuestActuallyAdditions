package net.yorunina.maa.mixin;

import dev.compactmods.gander.render.rendertypes.GanderCompositeRenderType;
import dev.compactmods.gander.render.rendertypes.RenderTypeStore;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gander assumes every render type passed through its wrapped buffer source is
 * one of its composite render types. Modern UI's text render type is a regular
 * RenderType, so that assumption causes a ClassCastException while rendering
 * Compact Machines screens.
 */
@Mixin(value = RenderTypeStore.class, remap = false)
public class MixinGanderRenderTypeStore {
    @Inject(method = "redirectedBlockRenderType", at = @At("HEAD"), cancellable = true, remap = false)
    private void maa$preserveNonGanderBlockRenderTypes(
            RenderType renderType,
            CallbackInfoReturnable<RenderType> cir
    ) {
        if (!(renderType instanceof GanderCompositeRenderType)) {
            cir.setReturnValue(renderType);
        }
    }

    @Inject(method = "redirectedFluidRenderType", at = @At("HEAD"), cancellable = true, remap = false)
    private void maa$preserveNonGanderFluidRenderTypes(
            RenderType renderType,
            CallbackInfoReturnable<RenderType> cir
    ) {
        if (!(renderType instanceof GanderCompositeRenderType)) {
            cir.setReturnValue(renderType);
        }
    }
}
