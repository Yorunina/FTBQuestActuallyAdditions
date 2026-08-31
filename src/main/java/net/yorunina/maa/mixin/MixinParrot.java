package net.yorunina.maa.mixin;

import net.minecraft.world.entity.animal.Parrot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Parrot.class)
public class MixinParrot {
    @ModifyConstant(method = "mobInteract", constant = @Constant(floatValue = Float.MAX_VALUE), require=0)
    public float modifyMobInteract(float constant) {
        return 1024F;
    }
}
