package net.yorunina.maa.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.yorunina.maa.model.ILivingEntityNumberPos;
import net.yorunina.maa.model.ILivingEntityWearingGold;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity implements ILivingEntityWearingGold, ILivingEntityNumberPos {
    @Unique
    private boolean isWearingGold = false;

    @Unique
    private int damageNumberPos = 0;

    @Override
    public boolean isWearingGold() {
        return isWearingGold;
    }

    @Override
    public void setWearingGold(boolean wearingGold) {
        isWearingGold = wearingGold;
    }

    @Unique
    public int getNextNumberPos() {
        return damageNumberPos++;
    }
}