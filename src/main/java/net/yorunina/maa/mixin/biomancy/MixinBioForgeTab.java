package net.yorunina.maa.mixin.biomancy;

import com.github.elenterius.biomancy.menu.BioForgeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BioForgeTab.class)
public abstract class MixinBioForgeTab {
    @ModifyVariable(method = "fromJson", at = @At("STORE"), ordinal = 0, remap = false)
    private static String maa$remapDeprecatedBlocksTab(String categoryId) {
        return "biomancy:blocks".equals(categoryId) ? "biomancy:misc" : categoryId;
    }
}