package net.yorunina.maa.items;

import dev.latvian.mods.itemfilters.item.StringValueData;
import dev.latvian.mods.itemfilters.item.StringValueFilterItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.tigereye.chestcavity.chestcavities.json.organs.OrganData;
import net.tigereye.chestcavity.util.ChestCavityUtil;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChestCavityOrganScoreFilterItem extends StringValueFilterItem {

    public static class OrganScoreCheck {
        public int mode;
        public ResourceLocation organScore;
        public float num;
    }

    public static class OrganScoreData extends StringValueData<OrganScoreCheck> {
        public OrganScoreData(ItemStack is) {
            super(is);
        }

        @Nullable
        @Override
        protected OrganScoreCheck fromString(String s) {

            Pattern pattern = Pattern.compile("([a-zA-Z:_]+)\\s*([<>=]+)\\s*([\\d\\.]+)");
            Matcher matcher = pattern.matcher(s.trim());

            if (matcher.matches()) {
                OrganScoreCheck check = new OrganScoreCheck();
                check.organScore = ResourceLocation.parse(matcher.group(1));
                String operator = matcher.group(2);
                check.num = Float.parseFloat(matcher.group(3));
                check.mode = RegistryItems.operation2Mode(operator);
                if (check.mode == -1) {
                    return null;
                }
                return check;
            }
            return null;
        }

        @Override
        protected String toString(OrganScoreCheck value) {
            if (value == null || value.organScore == null) return "";

            return value.organScore +
                    RegistryItems.mode2Operation(value.mode) +
                    value.num;
        }
    }

    @Override
    public StringValueData<?> createData(ItemStack stack) {
        return new OrganScoreData(stack);
    }

    @Override
    public boolean filter(ItemStack filter, ItemStack stack) {
        if (stack.isEmpty()) return false;

        // todo 未来架构重构中的变动点，需注意
        OrganData organData = ChestCavityUtil.lookupOrgan(stack, null);
        OrganScoreData data = getStringValueData(filter);

        if (data.getValue() == null || data.getValue().organScore == null) {
            return false;
        }
        float actualScore = organData.organScores.getOrDefault(data.getValue().organScore, 0f);
        float requireScore = data.getValue().num;

        return switch (data.getValue().mode) {
            case 1 -> actualScore >= requireScore;
            case 2 -> actualScore <= requireScore;
            case 3 -> actualScore > requireScore;
            case 4 -> actualScore < requireScore;
            case 0 -> actualScore == requireScore;
            default -> false;
        };
    }

    @Override
    public String getHelpKey() {
        return "itemfilters.help_text.chest_cavity_organ_score";
    }
}