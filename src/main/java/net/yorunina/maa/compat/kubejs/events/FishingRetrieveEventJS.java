package net.yorunina.maa.compat.kubejs.events;

import dev.latvian.mods.kubejs.player.PlayerEventJS;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class FishingRetrieveEventJS extends PlayerEventJS {
    private final ServerPlayer player;
    private final FishingHook fishingHook;
    private final List<ItemStack> items;

    public FishingRetrieveEventJS(ServerPlayer player, FishingHook fishingHook, List<ItemStack> items) {
        this.player = player;
        this.fishingHook = fishingHook;
        this.items = items;
    }

    @Override
    public ServerPlayer getEntity() {
        return player;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public FishingHook getFishingHook() {
        return fishingHook;
    }

    public List<ItemStack> getItems() {
        return items;
    }
}