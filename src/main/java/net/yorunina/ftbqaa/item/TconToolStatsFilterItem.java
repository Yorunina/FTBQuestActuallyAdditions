package net.yorunina.ftbqaa.item;

import dev.latvian.mods.itemfilters.item.StringValueData;
import dev.latvian.mods.itemfilters.item.StringValueFilterItem;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.IToolStat;
import slimeknights.tconstruct.library.tools.stat.ToolStatId;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TconToolStatsFilterItem extends StringValueFilterItem {

    public static class StatsCheck {
        public int mode; // 0: ==, 1: >=, 2: <=, 3: >, 4: <
        public IToolStat<?> stats;
        public float num;
    }

    public static class StatsData extends StringValueData<StatsCheck> {
        public StatsData(ItemStack is) {
            super(is);
        }

        @Nullable
        @Override
        protected StatsCheck fromString(String s) {

            Pattern pattern = Pattern.compile("([a-zA-Z:_]+)\\s*([<>=]+)\\s*([\\d\\.]+)");
            Matcher matcher = pattern.matcher(s.trim());

            if (matcher.matches()) {
                StatsCheck check = new StatsCheck();
                String statsIdStr = matcher.group(1);
                String operator = matcher.group(2);
                check.num = Float.parseFloat(matcher.group(3));

                if (!statsIdStr.contains(":")) {
                    statsIdStr = "tconstruct:" + statsIdStr;
                }
                ToolStatId statsId = new ToolStatId(statsIdStr);
                check.stats = ToolStats.getToolStat(statsId);

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

                return check;
            }
            return null; // No match
        }

        @Override
        protected String toString(StatsCheck value) {
            if (value == null || value.stats == null) {
                return "";
            }

            StringBuilder builder = new StringBuilder();
            builder.append(value.stats.getName()); // Get the full resource location of the modifier

            switch (value.mode) {
                case 1 -> builder.append(" >= ");
                case 2 -> builder.append(" <= ");
                case 3 -> builder.append(" > ");
                case 4 -> builder.append(" < ");
                case 0 -> builder.append(" == "); // Assuming 0 is for equality
            }

            builder.append(value.num);

            return builder.toString();
        }
    }

    @Override
    public StringValueData<?> createData(ItemStack stack) {
        return new StatsData(stack);
    }

    @Override
    public boolean filter(ItemStack filter, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        StatsData data = getStringValueData(filter);

        if (data.getValue() == null || data.getValue().stats == null) {
            return false;
        }
        ToolStack tool = ToolStack.from(stack);
        float actualModifierLevel = (float) tool.getStats().get(data.getValue().stats);
        float requiredLevel = data.getValue().num;

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
        return "itemfilters.help_text.tcon_tool_stats";
    }


}