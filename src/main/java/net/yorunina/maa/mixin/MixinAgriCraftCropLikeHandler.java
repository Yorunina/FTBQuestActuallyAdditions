package net.yorunina.maa.mixin;

import com.agricraft.agricraft.api.AgriApi;
import dev.ftb.mods.ftbultimine.integration.agricraft.AgriCraftCropLikeHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(value = AgriCraftCropLikeHandler.class)
public class MixinAgriCraftCropLikeHandler {
    @Inject(method = "isApplicable", at = @At("HEAD"), cancellable = true, remap = false)
    private void maa$guardEmptyCrop(
            Level level,
            BlockPos pos,
            BlockState state,
            CallbackInfoReturnable<Boolean> cir
    ) {
        AgriApi.getCrop(level, pos).ifPresent(crop -> {
            if (!crop.hasPlant() || crop.getGrowthStage() == null) {
                cir.setReturnValue(false);
            }
        });
    }
}
