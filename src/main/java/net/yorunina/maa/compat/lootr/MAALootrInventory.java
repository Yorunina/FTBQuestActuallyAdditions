package net.yorunina.maa.compat.lootr;

import net.minecraft.world.entity.player.Player;

public interface MAALootrInventory {
    boolean maa$shouldUseCustomMenu();

    int maa$prepareOpen(Player player);
}