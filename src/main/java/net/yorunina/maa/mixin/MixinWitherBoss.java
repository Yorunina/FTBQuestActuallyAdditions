package net.yorunina.maa.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nullable;

@Mixin(WitherBoss.class)
public abstract class MixinWitherBoss extends Monster {
    private MixinWitherBoss(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * @author Yorunina
     * @reason Allow harmful effects to be applied while preserving the vanilla
     *         behavior of rejecting beneficial and neutral effects.
     */
    @Overwrite
    public boolean addEffect(@NotNull MobEffectInstance effect, @Nullable Entity source) {
        return super.addEffect(effect, source);
    }
}
