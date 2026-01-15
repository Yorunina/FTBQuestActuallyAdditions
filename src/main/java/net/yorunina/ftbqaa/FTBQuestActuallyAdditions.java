package net.yorunina.ftbqaa;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.yorunina.ftbqaa.items.ItemFiltersItems;
import net.yorunina.ftbqaa.rewards.AARewardTypes;
import net.yorunina.ftbqaa.tasks.TasksRegistry;

@Mod(FTBQuestActuallyAdditions.MODID)
public class FTBQuestActuallyAdditions {

    public static final String MODID = "ftbqaa";

    public FTBQuestActuallyAdditions(FMLJavaModLoadingContext context) {
        IEventBus eventBus = context.getModEventBus();
        AARewardTypes.init();
        TasksRegistry.getInstance().init();
        ItemFiltersItems.register(eventBus);
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, name);
    }
}
