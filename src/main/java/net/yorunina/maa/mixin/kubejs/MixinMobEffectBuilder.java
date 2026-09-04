package net.yorunina.maa.mixin.kubejs;

import dev.latvian.mods.kubejs.misc.MobEffectBuilder;
import net.yorunina.maa.compat.kubejs.MAAMobEffectBuilderExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds the mob-effect builder API from KubeJS commit a62df230 to MAA's
 * runtime compatibility layer.
 */
@Mixin(value = MobEffectBuilder.class, remap = false)
public abstract class MixinMobEffectBuilder implements MAAMobEffectBuilderExtension {
    @Unique
    @Nullable
    public transient EffectChangeCallback addEffect;

    @Unique
    @Nullable
    public transient EffectChangeCallback removeEffect;

    @Override
    @Nullable
    public EffectChangeCallback maa$getAddEffect() {
        return addEffect;
    }

    @Override
    public void maa$setAddEffect(@Nullable EffectChangeCallback callback) {
        addEffect = callback;
    }

    @Override
    @Nullable
    public EffectChangeCallback maa$getRemoveEffect() {
        return removeEffect;
    }

    @Override
    public void maa$setRemoveEffect(@Nullable EffectChangeCallback callback) {
        removeEffect = callback;
    }

    @Unique
    public MobEffectBuilder addEffect(EffectChangeCallback callback) {
        maa$setAddEffect(callback);
        return (MobEffectBuilder) (Object) this;
    }

    @Unique
    public MobEffectBuilder removeEffect(EffectChangeCallback callback) {
        maa$setRemoveEffect(callback);
        return (MobEffectBuilder) (Object) this;
    }
}
