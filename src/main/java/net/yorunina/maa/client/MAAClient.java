package net.yorunina.maa.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.yorunina.maa.ModpackActuallyAdditions;
import net.yorunina.maa.client.screens.MAALootrChestScreen;
import net.yorunina.maa.registry.MAAMenus;

@Mod.EventBusSubscriber(modid = ModpackActuallyAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class MAAClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(MAAMenus.LOOTR_CHEST.get(), MAALootrChestScreen::new));
    }
}