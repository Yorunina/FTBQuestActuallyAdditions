package net.yorunina.ftbqaa.items;

import dev.latvian.mods.itemfilters.item.StringValueData;
import dev.latvian.mods.itemfilters.item.StringValueFilterItem;
import io.github.mortuusars.exposure.item.PhotographItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;

public class PhotoDimensionFilterItem extends StringValueFilterItem {

    public static class DimensionCheck {
        public String[] dimensions;
    }

    public static class DimensionData extends StringValueData<DimensionCheck> {
        public DimensionData(ItemStack is) {
            super(is);
        }

        @Nullable
        @Override
        protected DimensionCheck fromString(String s) {
            String[] dimensionList = s.split(";");

            if (dimensionList.length == 0) {
                return null;
            }
            DimensionCheck check = new DimensionCheck();
            check.dimensions = dimensionList;
            return check;
        }

        @Override
        protected String toString(@Nullable DimensionCheck dimensionCheck) {
            if (dimensionCheck == null || dimensionCheck.dimensions == null) {
                return "";
            }
            return String.join(";", dimensionCheck.dimensions);
        }
    }

    @Override
    public StringValueData<?> createData(ItemStack stack) {
        return new DimensionData(stack);
    }

    @Override
    public boolean filter(ItemStack filter, ItemStack stack) {
        Item item = stack.getItem();

        if (!(item instanceof PhotographItem)) return false;
        if (!stack.hasTag()) return false;
        CompoundTag nbt = stack.getTag();
        if (!nbt.contains("Dimension")) return false;

        DimensionData data = getStringValueData(filter);
        String[] dimensions = data.getValue().dimensions;
        if (dimensions.length == 0) return false;

        String dimStr = nbt.getString("Dimension");
        // 判断nbt中是否包含dimensions中的任意一个维度，只要前缀匹配即认为匹配
        return Arrays.stream(dimensions).anyMatch(dimStr::startsWith);
    }
    @Override
    public String getHelpKey() {
        return "itemfilters.help_text.exposure_photo_dimension";
    }
}