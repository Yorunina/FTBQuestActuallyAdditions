package net.yorunina.maa.compat.lootr;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public interface MAALootrChestData {
    boolean maa$isCustom();

    int maa$rememberPlayerRows(UUID playerId, int rows);

    void maa$loadPlayerRows(CompoundTag root);

    void maa$savePlayerRows(CompoundTag root);
}