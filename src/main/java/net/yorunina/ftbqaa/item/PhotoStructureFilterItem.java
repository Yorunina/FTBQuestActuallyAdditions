package net.yorunina.ftbqaa.item;

import dev.latvian.mods.itemfilters.item.StringValueData;
import dev.latvian.mods.itemfilters.item.StringValueFilterItem;
import io.github.mortuusars.exposure.item.PhotographItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;

public class PhotoStructureFilterItem extends StringValueFilterItem {

    public static class StructureCheck {
        public String[] structures;
    }

    public static class StructureData extends StringValueData<StructureCheck> {
        public StructureData(ItemStack is) {
            super(is);
        }

        @Nullable
        @Override
        protected StructureCheck fromString(String s) {
            String[] structureList = s.split(";");

            if (structureList.length == 0) {
                return null;
            }
            StructureCheck check = new StructureCheck();
            check.structures = structureList;
            return check;
        }

        @Override
        protected String toString(@Nullable StructureCheck structureCheck) {
            if (structureCheck == null || structureCheck.structures == null) {
                return "";
            }
            return String.join(";", structureCheck.structures);
        }
    }

    @Override
    public StringValueData<?> createData(ItemStack stack) {
        return new StructureData(stack);
    }

    @Override
    public boolean filter(ItemStack filter, ItemStack stack) {
        Item item = stack.getItem();

        if (!(item instanceof PhotographItem)) return false;
        if (!stack.hasTag()) return false;
        CompoundTag nbt = stack.getTag();
        if (!nbt.contains("Structures")) return false;

        StructureData data = getStringValueData(filter);
        String[] structures = data.getValue().structures;
        if (structures.length == 0) return false;

        ListTag structureNbtList = nbt.getList("Structures", Tag.TAG_STRING);
        // 判断nbt中是否包含structures中的任意一个结构

        return structureNbtList.stream()
                .map(Tag::getAsString)
                .filter(asString -> !asString.isEmpty())
                .anyMatch(Arrays.asList(structures)::contains);
    }
    @Override
    public String getHelpKey() {
        return "itemfilters.help_text.exposure_photo_structure";
    }
}