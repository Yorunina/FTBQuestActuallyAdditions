package net.yorunina.ftbqaa;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class FTBQAATags {

    public static final TagKey<Item> IMMUNE_CACTUS = itemTag("immune/cactus");
    public static final TagKey<Item> IMMUNE_EXPLOSION = itemTag("immune/explosion");
    public static final TagKey<Item> IMMUNE_FIRE = itemTag("immune/fire");
    public static final TagKey<Item> IMMUNE_LIGHTNING = itemTag("immune/lightning");

    private static TagKey<Item> itemTag(String id) {
        return tag(Registries.ITEM, id);
    }

    public static <T> TagKey<T> tag(ResourceKey<? extends Registry<T>> registryKey, String path) {
        return TagKey.create(registryKey, FTBQuestActuallyAdditions.id(path));
    }

}