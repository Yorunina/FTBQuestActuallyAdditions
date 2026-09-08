package net.yorunina.maa;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.ModList;
import net.yorunina.maa.compat.leaderboards.LeaderboardsCompat;
import net.yorunina.maa.items.RegistryItems;
import net.yorunina.maa.networks.MAAQuestNetHandler;
import net.yorunina.maa.registry.MAAGameRules;
import net.yorunina.maa.rewards.AARewardTypes;
import net.yorunina.maa.tasks.TasksRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.mortuusars.exposure.forge.RegisterImpl.CUSTOM_STATS;
import static net.yorunina.maa.registry.MAAStats.STATS;

@Mod(ModpackActuallyAdditions.MODID)
public class ModpackActuallyAdditions {
    public static final String MODID = "maa";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public ModpackActuallyAdditions(FMLJavaModLoadingContext context) {
        STATS.forEach((location, formatter) -> {
            CUSTOM_STATS.register(location.getPath(), () -> location);
        });
        MAAQuestNetHandler.init();
        MAAGameRules.init();
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonInit);
        if (FMLEnvironment.dist == Dist.CLIENT) modEventBus.addListener(this::clientInit);
        AARewardTypes.init();
        if (ModList.get().isLoaded("leaderboards") && ModList.get().isLoaded("ftbquests")) {
            LeaderboardsCompat.init();
        }
        RegistryItems.register(modEventBus);
    }

    private void commonInit(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (ModList.get().isLoaded("leaderboards") && ModList.get().isLoaded("ftbquests")) {
                LeaderboardsCompat.init();
            }
            TasksRegistry.getInstance().init();
            STATS.forEach((location, statFormatter) -> {
                Stats.CUSTOM.get(location);
            });
        });
    }

    private void clientInit(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, name);
    }
}
