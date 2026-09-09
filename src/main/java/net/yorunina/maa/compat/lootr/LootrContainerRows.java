package net.yorunina.maa.compat.lootr;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.yorunina.maa.registry.MAAAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LootrContainerRows {
    public static final int MIN_ROWS = 1;
    public static final int MAX_ROWS = 20;
    public static final int COLUMNS = 9;
    public static final int DEFAULT_ROWS = 3;
    public static final String ROWS_KEY = "maaLootrRows";
    public static final String PLAYER_ROWS_KEY = "maaLootrPlayerRows";
    public static final String PLAYER_KEY = "Player";

    private LootrContainerRows() {
    }

    public static int clampRows(int rows) {
        return Mth.clamp(rows, MIN_ROWS, MAX_ROWS);
    }

    public static int slotsFor(int rows) {
        return clampRows(rows) * COLUMNS;
    }

    public static int rowsFromSlots(int slots) {
        if (slots <= 0) {
            return DEFAULT_ROWS;
        }
        return clampRows((slots + COLUMNS - 1) / COLUMNS);
    }

    public static int rowsFromAttribute(Player player) {
        if (player == null) {
            return DEFAULT_ROWS;
        }
        AttributeInstance instance = player.getAttribute(MAAAttributes.LOOTR_CONTAINER_ROWS.get());
        if (instance == null) {
            return DEFAULT_ROWS;
        }
        return clampRows((int) Math.round(instance.getValue()));
    }

    public static int rowsFromTag(CompoundTag tag) {
        if (tag == null || !tag.contains(ROWS_KEY, Tag.TAG_INT)) {
            return -1;
        }
        return clampRows(tag.getInt(ROWS_KEY));
    }

    public static void writePlayerRows(CompoundTag root, Map<UUID, Integer> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Integer> entry : rows.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID(PLAYER_KEY, entry.getKey());
            tag.putInt(ROWS_KEY, clampRows(entry.getValue()));
            list.add(tag);
        }
        root.put(PLAYER_ROWS_KEY, list);
    }

    public static Map<UUID, Integer> readPlayerRows(CompoundTag root) {
        Map<UUID, Integer> result = new HashMap<>();
        if (root == null || !root.contains(PLAYER_ROWS_KEY, Tag.TAG_LIST)) {
            return result;
        }
        ListTag list = root.getList(PLAYER_ROWS_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < list.size(); index++) {
            CompoundTag tag = list.getCompound(index);
            if (!tag.hasUUID(PLAYER_KEY) || !tag.contains(ROWS_KEY, Tag.TAG_INT)) {
                continue;
            }
            result.put(tag.getUUID(PLAYER_KEY), clampRows(tag.getInt(ROWS_KEY)));
        }
        return result;
    }
}