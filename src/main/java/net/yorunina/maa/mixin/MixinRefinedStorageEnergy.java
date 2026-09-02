package net.yorunina.maa.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Refined Storage exposes an energy toggle for its controller and portable/
 * wireless grid items. Treat all of those toggles as disabled so the network
 * stays usable without an energy infrastructure.
 *
 * <p>The targets are named strings because Refined Storage is an optional
 * integration for MAA. {@link Pseudo} keeps this mixin inert when the mod is
 * not installed.</p>
 */
@Pseudo
@Mixin(targets = {
    "com.refinedmods.refinedstorage.config.ServerConfig$Controller",
    "com.refinedmods.refinedstorage.config.ServerConfig$WirelessGrid",
    "com.refinedmods.refinedstorage.config.ServerConfig$WirelessFluidGrid",
    "com.refinedmods.refinedstorage.config.ServerConfig$PortableGrid",
    "com.refinedmods.refinedstorage.config.ServerConfig$WirelessCraftingMonitor"
}, remap = false)
public abstract class MixinRefinedStorageEnergy {
    @ModifyReturnValue(method = "getUseEnergy", at = @At("RETURN"), remap = false)
    private boolean maa$disableEnergyRequirement(boolean original) {
        return false;
    }
}
