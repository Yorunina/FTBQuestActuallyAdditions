package net.yorunina.maa.compat.lootr;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.yorunina.maa.registry.MAAMenus;
import noobanidus.mods.lootr.api.MenuBuilder;

public final class MAALootrMenuBuilder implements MenuBuilder {
    @Override
    public AbstractContainerMenu build(int id, Inventory inventory, Container container, int rows) {
        return new MAALootrChestMenu(MAAMenus.LOOTR_CHEST.get(), id, inventory, container, rows);
    }
}