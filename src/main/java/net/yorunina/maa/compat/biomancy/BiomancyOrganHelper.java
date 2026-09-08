package net.yorunina.maa.compat.biomancy;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class BiomancyOrganHelper {
    public static final TagKey<Item> ORGAN = TagKey.create(Registries.ITEM, new ResourceLocation("kubejs", "organ"));
    public static final String ORGAN_COMPATIBILITY_TAG = "chestcavity:organ_compatibility";

    private BiomancyOrganHelper() {
    }

    public static boolean canHoldOrganItem(ItemStack stack) {
        return stack.is(ORGAN) && stack.hasTag() && stack.getTag().contains(ORGAN_COMPATIBILITY_TAG);
    }
}