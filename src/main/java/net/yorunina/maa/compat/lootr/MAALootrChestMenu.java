package net.yorunina.maa.compat.lootr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.yorunina.maa.registry.MAAMenus;

public class MAALootrChestMenu extends AbstractContainerMenu {
    private final Container container;
    private final int rows;
    private final int containerSize;

    public MAALootrChestMenu(MenuType<?> type, int containerId, Inventory playerInv, Container container, int rows) {
        super(type, containerId);
        this.rows = LootrContainerRows.clampRows(rows);
        this.containerSize = LootrContainerRows.slotsFor(this.rows);
        this.container = container;
        checkContainerSize(container, this.containerSize);

        for (int row = 0; row < this.rows; row++) {
            for (int col = 0; col < LootrContainerRows.COLUMNS; col++) {
                this.addSlot(new Slot(container, col + row * LootrContainerRows.COLUMNS, 8 + col * 18, 18 + row * 18));
            }
        }

        int playerInvY = 18 + this.rows * 18 + 14;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }
        int hotbarY = playerInvY + 58;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, hotbarY));
        }
        if (playerInv.player != null) {
            this.container.startOpen(playerInv.player);
        }
    }

    public static MAALootrChestMenu fromNetwork(int id, Inventory inv, FriendlyByteBuf data) {
        if (data == null || data.readableBytes() < 1) {
            throw new IllegalStateException("Missing Lootr chest extra data");
        }
        int rows = LootrContainerRows.clampRows(data.readVarInt());
        return new MAALootrChestMenu(MAAMenus.LOOTR_CHEST.get(), id, inv, new SimpleContainer(LootrContainerRows.slotsFor(rows)), rows);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            moved = stack.copy();
            int playerStart = containerSize;
            int playerEnd = containerSize + 36;
            if (slotIndex < playerStart) {
                if (!this.moveItemStackTo(stack, playerStart, playerEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, playerStart, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return moved;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public Container getContainer() {
        return this.container;
    }

    public int getRows() {
        return this.rows;
    }
}