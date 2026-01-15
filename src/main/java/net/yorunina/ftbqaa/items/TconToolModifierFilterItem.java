package net.yorunina.ftbqaa.items;

import dev.latvian.mods.itemfilters.item.StringValueData;
import dev.latvian.mods.itemfilters.item.StringValueFilterItem;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TconToolModifierFilterItem extends StringValueFilterItem {

    public static class ModifierCheck {
        public int mode; // 0: ==, 1: >=, 2: <=, 3: >, 4: <
        public ModifierId modifierId;
        public int level;
    }

    public static class ModifierData extends StringValueData<ModifierCheck> {
        public ModifierData(ItemStack is) {
            super(is);
        }

        @Nullable
        @Override
        protected ModifierCheck fromString(String s) {

            Pattern pattern = Pattern.compile("^([a-zA-Z:_]+)\\s*([<>=]+)\\s*(\\d+)$");
            Matcher matcher = pattern.matcher(s.trim());

            if (matcher.matches()) {
                String modifierId = matcher.group(1);
                String operator = matcher.group(2);
                int level = Integer.parseInt(matcher.group(3));

                ModifierCheck check = new ModifierCheck();

                if (!modifierId.contains(":")) {
                    modifierId = "tconstruct:" + modifierId;
                }

                check.modifierId = new ModifierId(modifierId);

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
                    case "=": // Assuming "=" means "=="
                    case "==":
                        check.mode = 0; // Default for equality
                        break;
                    default:
                        return null; // Unknown operator
                }

                check.level = level;
                return check;
            }
            return null; // No match
        }

        @Override
        protected String toString(ModifierCheck value) {
            if (value == null || value.modifierId == null) {
                return "";
            }

            StringBuilder builder = new StringBuilder();
            builder.append(value.modifierId); // Get the full resource location of the modifier

            switch (value.mode) {
                case 1 -> builder.append(" >= ");
                case 2 -> builder.append(" <= ");
                case 3 -> builder.append(" > ");
                case 4 -> builder.append(" < ");
                case 0 -> builder.append(" == "); // Assuming 0 is for equality
            }

            builder.append(value.level);

            return builder.toString();
        }
    }

    @Override
    public StringValueData<?> createData(ItemStack stack) {
        return new ModifierData(stack);
    }

    @Override
    public boolean filter(ItemStack filter, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        ModifierData data = getStringValueData(filter);

        if (data.getValue() == null || data.getValue().modifierId == null) {
            return false;
        }

        int actualModifierLevel = ModifierUtil.getModifierLevel(stack, data.getValue().modifierId);
        int requiredLevel = data.getValue().level;

        return switch (data.getValue().mode) {
            case 1 -> actualModifierLevel >= requiredLevel;
            case 2 -> actualModifierLevel <= requiredLevel;
            case 3 -> actualModifierLevel > requiredLevel;
            case 4 -> actualModifierLevel < requiredLevel;
            case 0 -> actualModifierLevel == requiredLevel;
            default -> false;
        };
    }

    @Override
    public String getHelpKey() {
        return "itemfilters.help_text.tcon_tool_modifier";
    }


}