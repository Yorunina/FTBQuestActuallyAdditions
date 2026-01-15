package net.yorunina.ftbqaa.items;

import dev.latvian.mods.itemfilters.item.StringValueData;
import dev.latvian.mods.itemfilters.item.StringValueFilterItem;
import io.github.mortuusars.exposure.item.PhotographItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhotoLightLevelFilterItem extends StringValueFilterItem {


    public static class LightLevelCheck {
        public int lightLevel;
        public int mode;
    }

    public static class LightLevelData extends StringValueData<LightLevelCheck> {
        public LightLevelData(ItemStack is) {
            super(is);
        }

        @Nullable
        @Override
        protected LightLevelCheck fromString(String s) {
            LightLevelCheck check = new LightLevelCheck();
            Pattern pattern = Pattern.compile("^([<>=]+)\\s*(\\d+)$");
            Matcher matcher = pattern.matcher(s.trim());
            if (matcher.matches()) {
                String operator = matcher.group(1);
                check.lightLevel = Integer.parseInt(matcher.group(2));
                switch (operator) {
                    case ">=":
                        check.mode = 1;
                        break;
                    case "<=":
                        check.mode = 2;
                        break;
                    case ">":
                        check.mode = 3;
                        break;
                    case "<":
                        check.mode = 4;
                        break;
                    case "=":
                    case "==":
                        check.mode = 0;
                        break;
                }
            }
            return check;
        }

        @Override
        protected String toString(@Nullable LightLevelCheck lightLevelCheck) {
            if (lightLevelCheck == null) {
                return "";
            }
            StringBuilder builder = new StringBuilder();
            switch (lightLevelCheck.mode) {
                case 1 -> builder.append(" >= ");
                case 2 -> builder.append(" <= ");
                case 3 -> builder.append(" > ");
                case 4 -> builder.append(" < ");
                case 0 -> builder.append(" == "); // Assuming 0 is for equality
            }
            builder.append(lightLevelCheck.lightLevel);
            return builder.toString();
        }
    }

    @Override
    public StringValueData<?> createData(ItemStack stack) {
        return new LightLevelData(stack);
    }

    @Override
    public boolean filter(ItemStack filter, ItemStack stack) {
        Item item = stack.getItem();

        if (!(item instanceof PhotographItem)) return false;
        if (!stack.hasTag()) return false;
        CompoundTag nbt = stack.getTag();
        if (!nbt.contains("LightLevel")) return false;

        LightLevelData data = getStringValueData(filter);
        LightLevelCheck lightLevelCheck = data.getValue();

        int lightLevel = nbt.getInt("LightLevel");

        boolean result = true;
        switch (lightLevelCheck.mode) {
            case 1 -> result &= lightLevel >= lightLevelCheck.lightLevel;
            case 2 -> result &= lightLevel <= lightLevelCheck.lightLevel;
            case 3 -> result &= lightLevel > lightLevelCheck.lightLevel;
            case 4 -> result &= lightLevel < lightLevelCheck.lightLevel;
            case 0 -> result &= lightLevel == lightLevelCheck.lightLevel;
            default -> result = false;
        }

        return result;
    }

    @Override
    public String getHelpKey() {
        return "itemfilters.help_text.photo_light_level";
    }
}