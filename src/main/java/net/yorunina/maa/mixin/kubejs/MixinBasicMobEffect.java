package net.yorunina.maa.mixin.kubejs;

import dev.latvian.mods.kubejs.misc.BasicMobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.yorunina.maa.compat.kubejs.MAAMobEffectBuilderExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies MAA's mob-effect callbacks at the same point as KubeJS commit
 * a62df230: after the effect's attribute map has been materialized and before
 * vanilla attribute add/remove work.
 */
@Mixin(value = BasicMobEffect.class, remap = false)
public abstract class MixinBasicMobEffect {
    @Unique
    @Nullable
    private transient MAAMobEffectBuilderExtension.EffectChangeCallback maa$addEffect;

    @Unique
    @Nullable
    private transient MAAMobEffectBuilderExtension.EffectChangeCallback maa$removeEffect;

    @Shadow
    private void applyAttributeModifications() {
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void maa$init(BasicMobEffect.Builder builder, CallbackInfo ci) {
        if (builder instanceof MAAMobEffectBuilderExtension extension) {
            maa$addEffect = extension.maa$getAddEffect();
            maa$removeEffect = extension.maa$getRemoveEffect();
        }
    }

    @Inject(method = "removeAttributeModifiers", at = @At("HEAD"), remap = false)
    private void maa$removeAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int level, CallbackInfo ci) {
        applyAttributeModifications();
        if (maa$removeEffect != null) {
            maa$removeEffect.applyEffectChange(livingEntity, attributeMap, level);
        }
    }

    @Inject(method = "addAttributeModifiers", at = @At("HEAD"), remap = false)
    private void maa$addAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int level, CallbackInfo ci) {
        applyAttributeModifications();
        if (maa$addEffect != null) {
            maa$addEffect.applyEffectChange(livingEntity, attributeMap, level);
        }
    }
}
