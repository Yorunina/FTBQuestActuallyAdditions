package net.yorunina.maa.mixin;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconAnimation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.yorunina.maa.model.IIconAnimation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.List;

@Mixin(IconAnimation.class)
public abstract class MixinIconAnimation extends Icon implements IIconAnimation {
    @Unique
    private long frameLength = 1000L;

    @Mutable
    @Final
    @Shadow
    public List<Icon> list;

    @OnlyIn(Dist.CLIENT)
    @ModifyConstant(method = "draw", constant = @Constant(longValue = 1000L), remap = false)
    private long draw(long constant) {
        return this.frameLength;
    }

    @OnlyIn(Dist.CLIENT)
    @ModifyConstant(method = "draw3D", constant = @Constant(longValue = 1000L), remap = false)
    private long draw3D(long constant) {
        return this.frameLength;
    }

    @Unique
    public void setFrameLength(long frameLength) {
        this.frameLength = frameLength;
    }
}
