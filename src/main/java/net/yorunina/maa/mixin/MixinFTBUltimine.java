package net.yorunina.maa.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * todo 扩展判断条件
 */
@Pseudo
@Mixin(targets = "dev.ftb.mods.ftbultimine.FTBUltimine", remap = false)
public abstract class MixinFTBUltimine {

    @ModifyReturnValue(method = "canUltimine", at = @At("RETURN"), remap = false)
    private boolean maa$checkKubeEvent(boolean original, Player player) {
        return original;
    }
}
