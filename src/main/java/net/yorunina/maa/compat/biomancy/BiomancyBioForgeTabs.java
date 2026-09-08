package net.yorunina.maa.compat.biomancy;

import com.github.elenterius.biomancy.init.ModBioForgeTabs;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.menu.BioForgeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class BiomancyBioForgeTabs {
    public static final DeferredRegister<BioForgeTab> TABS = DeferredRegister.create(ModBioForgeTabs.REGISTRY_KEY, "biomancy");
    public static final RegistryObject<BioForgeTab> ORGAN = TABS.register("organ", () -> new BioForgeTab(98, ModItems.PRIMORDIAL_CORE.get()));

    private BiomancyBioForgeTabs() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}