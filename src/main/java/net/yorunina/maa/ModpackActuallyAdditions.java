package net.yorunina.maa;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.yorunina.maa.items.ItemFiltersItems;
import net.yorunina.maa.rewards.AARewardTypes;
import net.yorunina.maa.tasks.TasksRegistry;

@Mod(ModpackActuallyAdditions.MODID)
public class ModpackActuallyAdditions {
    public static final String MODID = "maa";
    public ModpackActuallyAdditions(FMLJavaModLoadingContext context) {
        context.getModEventBus().addListener(this::preInit);
        IEventBus eventBus = context.getModEventBus();
        AARewardTypes.init();
        ItemFiltersItems.register(eventBus);
    }

    private void preInit(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            TasksRegistry.getInstance().init();
        });
    }
    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, name);
    }
}
