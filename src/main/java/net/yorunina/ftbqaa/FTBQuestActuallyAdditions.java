package net.yorunina.ftbqaa;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.yorunina.ftbqaa.item.ItemFiltersItems;
import net.yorunina.ftbqaa.rewards.AARewardTypes;
import net.yorunina.ftbqaa.tasks.AATaskTypes;
import net.yorunina.ftbqaa.tasks.FTBQAAEventHandler;

@Mod(FTBQuestActuallyAdditions.MODID)
public class FTBQuestActuallyAdditions {

    public static final String MODID = "ftbqaa";
    private static final FTBQAAEventHandler FTB_EVENT_HANDLER = new FTBQAAEventHandler();

    public FTBQuestActuallyAdditions(FMLJavaModLoadingContext context) {
        IEventBus eventBus = context.getModEventBus();
        AARewardTypes.init();
        AATaskTypes.init();
        FTB_EVENT_HANDLER.init();
        ItemFiltersItems.register(eventBus);
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, name);
    }
}
