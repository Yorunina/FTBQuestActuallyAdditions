package net.yorunina.maa.registry;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yorunina.maa.ModpackActuallyAdditions;
import net.yorunina.maa.compat.lootr.MAALootrChestMenu;

public class MAAMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ModpackActuallyAdditions.MODID);

    public static final RegistryObject<MenuType<MAALootrChestMenu>> LOOTR_CHEST =
            MENUS.register("lootr_chest", () -> IForgeMenuType.create(MAALootrChestMenu::fromNetwork));
}