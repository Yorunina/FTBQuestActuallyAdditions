package net.yorunina.maa.mixin.lootr;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.yorunina.maa.compat.lootr.LootrContainerRows;
import net.yorunina.maa.compat.lootr.MAALootrChestData;
import noobanidus.mods.lootr.api.LootFiller;
import noobanidus.mods.lootr.data.ChestData;
import noobanidus.mods.lootr.data.SpecialChestInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

@Pseudo
@Mixin(value = ChestData.class, remap = false)
public abstract class MixinChestData implements MAALootrChestData {
    @Unique
    private final Map<UUID, Integer> maa$playerRows = new HashMap<>();

    @Shadow
    private boolean custom;

    @Shadow
    private int size;

    @Override
    public boolean maa$isCustom() {
        return this.custom;
    }

    @Override
    public int maa$rememberPlayerRows(UUID playerId, int rows) {
        int clamped = LootrContainerRows.clampRows(rows);
        Integer previous = this.maa$playerRows.putIfAbsent(playerId, clamped);
        int stored = previous == null ? clamped : previous;
        int slots = LootrContainerRows.slotsFor(stored);
        if (slots > this.size) {
            this.size = slots;
        }
        if (previous == null) {
            ((ChestData) (Object) this).setDirty();
        }
        return stored;
    }

    @Override
    public void maa$loadPlayerRows(CompoundTag root) {
        this.maa$playerRows.clear();
        this.maa$playerRows.putAll(LootrContainerRows.readPlayerRows(root));
    }

    @Override
    public void maa$savePlayerRows(CompoundTag root) {
        LootrContainerRows.writePlayerRows(root, this.maa$playerRows);
    }

    @Unique
    private NonNullList<ItemStack> maa$allocateForPlayer(ServerPlayer player, int fallbackSize) {
        if (this.custom) {
            return NonNullList.withSize(fallbackSize, ItemStack.EMPTY);
        }
        UUID playerId = player.getUUID();
        Integer existing = this.maa$playerRows.get(playerId);
        int rows = existing != null ? existing : LootrContainerRows.rowsFromAttribute(player);
        rows = maa$rememberPlayerRows(playerId, rows);
        return NonNullList.withSize(LootrContainerRows.slotsFor(rows), ItemStack.EMPTY);
    }

    @Redirect(
            method = "createInventory(Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/api/LootFiller;Lnet/minecraft/world/level/block/entity/RandomizableContainerBlockEntity;)Lnoobanidus/mods/lootr/data/SpecialChestInventory;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;withSize(ILjava/lang/Object;)Lnet/minecraft/core/NonNullList;", remap = true)
    )
    private NonNullList<ItemStack> maa$allocateRandomizable(int size, Object empty, ServerPlayer player, LootFiller filler, RandomizableContainerBlockEntity tile) {
        return maa$allocateForPlayer(player, size);
    }

    @Redirect(
            method = "createInventory(Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/api/LootFiller;Lnet/minecraft/world/level/block/entity/BaseContainerBlockEntity;Ljava/util/function/Supplier;Ljava/util/function/LongSupplier;)Lnoobanidus/mods/lootr/data/SpecialChestInventory;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;withSize(ILjava/lang/Object;)Lnet/minecraft/core/NonNullList;", remap = true)
    )
    private NonNullList<ItemStack> maa$allocateBase(int size, Object empty, ServerPlayer player, LootFiller filler, BaseContainerBlockEntity blockEntity, Supplier<ResourceLocation> tableSupplier, LongSupplier seedSupplier) {
        return maa$allocateForPlayer(player, size);
    }

    @Inject(method = "load(Lnet/minecraft/nbt/CompoundTag;)Lnoobanidus/mods/lootr/data/ChestData;", at = @At("RETURN"))
    private static void maa$loadPlayerRows(CompoundTag compound, CallbackInfoReturnable<ChestData> cir) {
        if (cir.getReturnValue() instanceof MAALootrChestData access) {
            access.maa$loadPlayerRows(compound);
        }
    }

    @Inject(method = "save(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"))
    private void maa$savePlayerRows(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
        if (cir.getReturnValue() != null) {
            maa$savePlayerRows(cir.getReturnValue());
        }
    }
}