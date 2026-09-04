package net.yorunina.maa.compat.kubejs;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.jetbrains.annotations.Nullable;

/**
 * MAA-owned state used to extend KubeJS' MobEffectBuilder without changing
 * KubeJS sources.
 */
public interface MAAMobEffectBuilderExtension {
    @Nullable
    EffectChangeCallback maa$getAddEffect();

    void maa$setAddEffect(@Nullable EffectChangeCallback callback);

    @Nullable
    EffectChangeCallback maa$getRemoveEffect();

    void maa$setRemoveEffect(@Nullable EffectChangeCallback callback);

    @FunctionalInterface
    interface EffectChangeCallback {
        void applyEffectChange(LivingEntity livingEntity, AttributeMap attributeMap, int level);
    }
}
