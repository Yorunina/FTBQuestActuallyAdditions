package net.yorunina.maa.mixin.lootr;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yorunina.maa.compat.lootr.LootrContainerRows;
import net.yorunina.maa.compat.lootr.MAALootrChestData;
import net.yorunina.maa.compat.lootr.MAALootrInventory;
import net.yorunina.maa.compat.lootr.MAALootrMenuBuilder;
import noobanidus.mods.lootr.api.MenuBuilder;
import noobanidus.mods.lootr.data.ChestData;
import noobanidus.mods.lootr.data.SpecialChestInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SpecialChestInventory.class, remap = false)
public abstract class MixinSpecialChestInventory implements MAALootrInventory {
    @Unique
    private int maa$rows;

    @Shadow
    private NonNullList<ItemStack> contents;

    @Shadow
    @Final
    private ChestData newChestData;

    @Shadow
    private MenuBuilder menuBuilder;

    @Shadow
    public abstract void setMenuBuilder(MenuBuilder builder);

    @Override
    public boolean maa$shouldUseCustomMenu() {
        if (this.menuBuilder != null && !(this.menuBuilder instanceof MAALootrMenuBuilder)) {
            return false;
        }
        return !(this.newChestData instanceof MAALootrChestData data) || !data.maa$isCustom();
    }

    @Override
    public int maa$prepareOpen(Player player) {
        int rows = maa$resolveRows();
        if (this.newChestData instanceof MAALootrChestData data) {
            rows = data.maa$rememberPlayerRows(player.getUUID(), rows);
        }
        maa$finishInit(rows);
        return rows;
    }

    @Unique
    private int maa$resolveRows() {
        if (this.maa$rows >= LootrContainerRows.MIN_ROWS) {
            return LootrContainerRows.clampRows(this.maa$rows);
        }
        return LootrContainerRows.rowsFromSlots(this.contents == null ? 0 : this.contents.size());
    }

    @Unique
    private void maa$finishInit(int rows) {
        this.maa$rows = LootrContainerRows.clampRows(rows);
        if (maa$shouldUseCustomMenu() && this.menuBuilder == null) {
            setMenuBuilder(new MAALootrMenuBuilder());
        }
    }

    @Redirect(
            method = "<init>(Lnoobanidus/mods/lootr/data/ChestData;Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;withSize(ILjava/lang/Object;)Lnet/minecraft/core/NonNullList;", remap = true)
    )
    private NonNullList<ItemStack> maa$allocateLoadedContents(int size, Object empty, ChestData chestData, CompoundTag items, String name) {
        int rows = LootrContainerRows.rowsFromTag(items);
        if (rows > 0) {
            this.maa$rows = rows;
            return NonNullList.withSize(LootrContainerRows.slotsFor(rows), ItemStack.EMPTY);
        }
        return NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Inject(
            method = "<init>(Lnoobanidus/mods/lootr/data/ChestData;Lnet/minecraft/nbt/CompoundTag;Ljava/lang/String;)V",
            at = @At("RETURN")
    )
    private void maa$afterLoaded(ChestData chestData, CompoundTag items, String name, CallbackInfo ci) {
        maa$finishInit(maa$resolveRows());
    }

    @Inject(
            method = "<init>(Lnoobanidus/mods/lootr/data/ChestData;Lnet/minecraft/core/NonNullList;Lnet/minecraft/network/chat/Component;)V",
            at = @At("RETURN")
    )
    private void maa$afterCreated(ChestData chestData, NonNullList<ItemStack> items, Component name, CallbackInfo ci) {
        maa$finishInit(maa$resolveRows());
    }

    @Inject(method = "writeItems()Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"))
    private void maa$writeRows(CallbackInfoReturnable<CompoundTag> cir) {
        if (cir.getReturnValue() != null) {
            cir.getReturnValue().putInt(LootrContainerRows.ROWS_KEY, maa$resolveRows());
        }
    }
}