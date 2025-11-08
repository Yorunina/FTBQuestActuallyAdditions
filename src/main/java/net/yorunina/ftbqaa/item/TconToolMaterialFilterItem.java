package net.yorunina.ftbqaa.item;

import com.google.common.base.Joiner;
import dev.latvian.mods.itemfilters.item.StringValueData;
import dev.latvian.mods.itemfilters.item.StringValueFilterItem;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TconToolMaterialFilterItem extends StringValueFilterItem {

    public static class MaterialCheck {
        public MaterialId materialId;
        public List<Integer> partIds = List.of();
    }

    public static class MaterialData extends StringValueData<MaterialCheck> {
        public MaterialData(ItemStack is) {
            super(is);
        }

        @Nullable
        @Override
        protected MaterialCheck fromString(String s) {
            Pattern pattern = Pattern.compile("([a-zA-Z:_]+)\\s*;?\\s*([\\d,]+)?");
            Matcher matcher = pattern.matcher(s.trim());
            if (matcher.matches()) {
                MaterialCheck check = new MaterialCheck();
                String materialId = matcher.group(1);
                String matchGroup = matcher.group(2);
                if (matchGroup != null) {
                    String[] partIdStrList = matcher.group(2).split(",");
                    if (partIdStrList.length > 0) {
                        check.partIds = Arrays.stream(partIdStrList).map(Integer::parseInt).toList();
                    }
                }
                if (!materialId.contains(":")) {
                    materialId = "tconstruct:" + materialId;
                }

                check.materialId = new MaterialId(materialId);

                return check;
            }
            return null; // No match
        }

        @Override
        protected String toString(MaterialCheck value) {
            if (value == null || value.materialId == null) {
                return "";
            }
            StringBuilder builder = new StringBuilder();
            builder.append(value.materialId); // Get the full resource location of the modifier
            if (!value.partIds.isEmpty()) {
                builder.append(";");
                builder.append(Joiner.on(";").join(value.partIds));
            }

            return builder.toString();
        }
    }

    @Override
    public StringValueData<?> createData(ItemStack stack) {
        return new MaterialData(stack);
    }

    @Override
    public boolean filter(ItemStack filter, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        MaterialData data = getStringValueData(filter);

        if (data.getValue() == null) {
            return false;
        }
        MaterialId materialId = data.getValue().materialId;
        if (materialId == null) {
            return false;
        }
        ToolStack tool = ToolStack.from(stack);
        List<Integer> partIds = data.getValue().partIds;

        if (!partIds.isEmpty()) {
            for (int partId : partIds) {
                if (!tool.getMaterial(partId).matches(materialId)) {
                    return false;
                }
            }
        } else {
            for (MaterialVariant materialVariant : tool.getMaterials().getList()) {
                if (materialVariant.matches(materialId)) {
                    return true;
                }
            }
            return false;
        }


        return true;
    }

    @Override
    public String getHelpKey() {
        return "itemfilters.help_text.tcon_tool_material";
    }


}