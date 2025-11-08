package net.yorunina.ftbqaa.item;

import dev.latvian.mods.itemfilters.item.StringValueData;
import dev.latvian.mods.itemfilters.item.StringValueFilterItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class DurabilityFilterItem extends StringValueFilterItem {
    public StringValueData<?> createData(ItemStack stack) {
        return new DurabilityData(stack);
    }

    public static class DurabilityCheck {
        public int mode;
        public int durability;
    }

    public static class DurabilityData extends StringValueData<DurabilityCheck> {
        public DurabilityData(ItemStack is) {
            super(is);
        }

        @Nullable
        protected DurabilityCheck fromString(String s) {
            s = s.replaceAll("\\s", "");

            try {
                DurabilityCheck check = new DurabilityCheck();
                if (s.startsWith(">=")) {
                    check.mode = 1;
                    s = s.substring(2);
                } else if (s.startsWith("<=")) {
                    check.mode = 2;
                    s = s.substring(2);
                } else if (s.startsWith(">")) {
                    check.mode = 3;
                    s = s.substring(1);
                } else if (s.startsWith("<")) {
                    check.mode = 4;
                    s = s.substring(1);
                }

                check.durability = Integer.parseInt(s);
                return check;
            } catch (Exception var3) {
                return null;
            }
        }

        protected String toString(DurabilityCheck value) {
            if (value == null) {
                return "";
            } else {
                StringBuilder builder = new StringBuilder();
                switch (value.mode) {
                    case 1 -> builder.append(">=");
                    case 2 -> builder.append("<=");
                    case 3 -> builder.append(">");
                    case 4 -> builder.append("<");
                }

                builder.append(value.durability);
                return builder.toString();
            }
        }
    }

    public boolean filter(ItemStack filter, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        } else {
            DurabilityData data = this.getStringValueData(filter);
            if (data.getValue() == null) {
                return false;
            } else {
                int d1 = stack.getMaxDamage();
                int d2 = data.getValue().durability;
                boolean mode;
                switch (data.getValue().mode) {
                    case 1 -> mode = d1 >= d2;
                    case 2 -> mode = d1 <= d2;
                    case 3 -> mode = d1 > d2;
                    case 4 -> mode = d1 < d2;
                    default -> mode = d1 == d2;
                }

                return mode;
            }
        }
    }

    public String getHelpKey() {
        return "itemfilters.help_text.durability";
    }
}
