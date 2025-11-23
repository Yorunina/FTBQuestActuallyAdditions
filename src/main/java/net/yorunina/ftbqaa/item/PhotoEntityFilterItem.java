package net.yorunina.ftbqaa.item;

import com.google.common.base.Joiner;
import dev.latvian.mods.itemfilters.item.StringValueData;
import dev.latvian.mods.itemfilters.item.StringValueFilterItem;
import io.github.mortuusars.exposure.item.PhotographItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.ModifierId;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhotoEntityFilterItem extends StringValueFilterItem {

    public static class EntityPhotoData {
        public String entityId;
        public float distance;
        public int mode;
    }

    public static class EntityCheck {
        public List<EntityPhotoData> entities = new ArrayList<>();
    }

    public static class EntityData extends StringValueData<EntityCheck> {
        public EntityData(ItemStack is) {
            super(is);
        }

        @Nullable
        @Override
        protected EntityCheck fromString(String s) {
            String[] entityDataList = s.split(";");

            if (entityDataList.length == 0) {
                return null;
            }
            EntityCheck entityCheck = new EntityCheck();
            entityCheck.entities = Arrays.stream(entityDataList).map(
                    entityDataStr -> {
                        EntityPhotoData check = new EntityPhotoData();
                        Pattern pattern = Pattern.compile("^([a-zA-Z:_]+)\\s*([<>=]+)\\s*(\\d+\\.?\\d*)$");
                        Matcher matcher = pattern.matcher(entityDataStr.trim());
                        if (matcher.matches()) {
                            String entityId = matcher.group(1);
                            String operator = matcher.group(2);
                            float distance = Float.parseFloat(matcher.group(3));
                            check.entityId = entityId;
                            check.distance = distance;
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
                            }
                        }
                        return check;
                    }
            ).toList();
            return entityCheck;
        }

        @Override
        protected String toString(@Nullable EntityCheck structureCheck) {
            if (structureCheck == null || structureCheck.entities == null) {
                return "";
            }
            StringBuilder builder = new StringBuilder();
            structureCheck.entities.forEach(entityCheck -> {
                builder.append(entityCheck.entityId);
                switch (entityCheck.mode) {
                    case 1 -> builder.append(" >= ");
                    case 2 -> builder.append(" <= ");
                    case 3 -> builder.append(" > ");
                    case 4 -> builder.append(" < ");
                    case 0 -> builder.append(" == "); // Assuming 0 is for equality
                }
                builder.append(entityCheck.distance);
            });

            return builder.toString();
        }
    }

    @Override
    public StringValueData<?> createData(ItemStack stack) {
        return new EntityData(stack);
    }

    @Override
    public boolean filter(ItemStack filter, ItemStack stack) {
        Item item = stack.getItem();

        if (!(item instanceof PhotographItem)) return false;
        if (!stack.hasTag()) return false;
        CompoundTag nbt = stack.getTag();
        if (!nbt.contains("Entities")) return false;

        EntityData data = getStringValueData(filter);
        List<EntityPhotoData> entityChecks = data.getValue().entities;
        if (entityChecks.isEmpty()) return false;

        ListTag entityNbtList = nbt.getList("Entities", Tag.TAG_COMPOUND);

        if (entityNbtList.isEmpty()) return false;
        for (Tag entityTag : entityNbtList) {
            if (entityTag instanceof CompoundTag entityNbt && entityNbt.contains("Id")) {
                String entityId = entityNbt.getString("Id");
                float distance = entityNbt.getFloat("Distance");
                if (entityChecks.stream()
                        .anyMatch(entityPhotoData -> entityPhotoData.entityId.equals(entityId) &&
                                (switch (entityPhotoData.mode) {
                                    case 1 -> distance >= entityPhotoData.distance;
                                    case 2 -> distance <= entityPhotoData.distance;
                                    case 3 -> distance > entityPhotoData.distance;
                                    case 4 -> distance < entityPhotoData.distance;
                                    case 0 -> distance == entityPhotoData.distance;
                                    default -> false;
                                }))) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String getHelpKey() {
        return "itemfilters.help_text.exposure_photo_entity";
    }
}