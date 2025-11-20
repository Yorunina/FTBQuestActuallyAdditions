package net.yorunina.ftbqaa.mixin;

import com.alessandro.astages.api.AStagesUtils;
import com.alessandro.astages.api.holder.AHolder;
import dev.ftb.mods.ftblibrary.integration.stages.StageProvider;
import dev.ftb.mods.ftbquests.block.entity.StageBarrierBlockEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({StageBarrierBlockEntity.class})
public class MixinStageBarrierBlockEntity {
    @Redirect(
            method = "isOpen",
            at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftblibrary/integration/stages/StageProvider;has(Lnet/minecraft/world/entity/player/Player;Ljava/lang/String;)Z")
    )
    public boolean stageHas(StageProvider instance, Player player, String s) {
        return AStagesUtils.hasStage(AHolder.serverAndPlayer(player), s);
    }
}
