package net.yorunina.ftbqaa.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yorunina.ftbqaa.FTBQuestActuallyAdditions;

@Mod.EventBusSubscriber(modid = FTBQuestActuallyAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ItemFiltersItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FTBQuestActuallyAdditions.MODID);
    public static final RegistryObject<Item> DURABILITY_ITEM_FILTER = ITEMS.register("durability", DurabilityFilterItem::new);
    public static final RegistryObject<Item> TCON_TOOL_MODIFIER_FILTER = ITEMS.register("tcon_tool_modifier",
            TconToolModifierFilterItem::new);
    public static final RegistryObject<Item> TCON_TOOL_MATERIAL_FILTER = ITEMS.register("tcon_tool_material",
            TconToolMaterialFilterItem::new);
    public static final RegistryObject<Item> TCON_TOOL_STATS_FILTER = ITEMS.register("tcon_tool_stats",
            TconToolStatsFilterItem::new);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
