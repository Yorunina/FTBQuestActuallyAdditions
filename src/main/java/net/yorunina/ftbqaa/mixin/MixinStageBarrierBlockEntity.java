package net.yorunina.ftbqaa.mixin;

import com.alessandro.astages.api.AStagesUtils;
import com.alessandro.astages.api.holder.AHolder;
import dev.ftb.mods.ftbquests.block.entity.StageBarrierBlockEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StageBarrierBlockEntity.class, remap = false)
public class MixinStageBarrierBlockEntity {
    @Shadow
    private String stage;
    @Inject(method = "isOpen", at = @At("HEAD"), cancellable = true)
    public void isOpen(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!this.stage.isEmpty() && AStagesUtils.hasStage(AHolder.serverAndPlayer(player), this.stage)) {
            cir.setReturnValue(true);
        }
    }
}