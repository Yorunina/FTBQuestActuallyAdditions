package net.yorunina.ftbqaa.items;

import dev.latvian.mods.itemfilters.item.StringValueData;
import dev.latvian.mods.itemfilters.item.StringValueFilterItem;
import io.github.mortuusars.exposure.item.PhotographItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhotoEntityFilterItem extends StringValueFilterItem {

    public static class EntityPhotoData {
        public String entityId;
        public int count;
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
                            int count = Integer.parseInt(matcher.group(3));
                            check.entityId = entityId;
                            check.count = count;
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
                builder.append(entityCheck.count);
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

        Map<String, Integer> entityCountMap = new HashMap<>();
        for (Tag entityTag : entityNbtList) {
            if (entityTag instanceof CompoundTag entityNbt && entityNbt.contains("Id")) {
                String entityId = entityNbt.getString("Id");
                entityCountMap.put(entityId, entityCountMap.getOrDefault(entityId, 0) + 1);
            }
        }

        boolean result = true;
        for (EntityPhotoData entityCheck : entityChecks) {
            String entityId = entityCheck.entityId;
            int count = entityCountMap.getOrDefault(entityId, 0);
            switch (entityCheck.mode) {
                case 1 -> result &= count >= entityCheck.count;
                case 2 -> result &= count <= entityCheck.count;
                case 3 -> result &= count > entityCheck.count;
                case 4 -> result &= count < entityCheck.count;
                case 0 -> result &= count == entityCheck.count;
                default -> result = false;
            }
        }

        return result;
    }

    @Override
    public String getHelpKey() {
        return "itemfilters.help_text.exposure_photo_entity";
    }
}