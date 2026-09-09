package net.yorunina.maa.mixin.biomancy;

import com.github.elenterius.biomancy.init.ModBioForgeTabs;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.menu.BioForgeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

@Mixin(ModBioForgeTabs.class)
public abstract class MixinModBioForgeTabs {
    @Redirect(
            method = "register(Ljava/lang/String;ILjava/util/function/Supplier;)Lnet/minecraftforge/registries/RegistryObject;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/registries/DeferredRegister;register(Ljava/lang/String;Ljava/util/function/Supplier;)Lnet/minecraftforge/registries/RegistryObject;"
            ),
            remap = false
    )
    private static RegistryObject<BioForgeTab> maa$replaceBlocksTabWithOrgan(DeferredRegister<BioForgeTab> registry, String name, Supplier<BioForgeTab> supplier) {
        if ("blocks".equals(name)) {
            return registry.register("organ", () -> new BioForgeTab(98, ModItems.PRIMORDIAL_CORE.get()));
        }
        return registry.register(name, supplier);
    }
}