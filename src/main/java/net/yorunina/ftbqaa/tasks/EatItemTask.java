package net.yorunina.ftbqaa.tasks;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconAnimation;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import dev.ftb.mods.ftbquests.net.FTBQuestsNetHandler;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.util.NBTUtils;
import dev.latvian.mods.itemfilters.api.IItemFilter;
import dev.latvian.mods.itemfilters.api.ItemFiltersAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public class EatItemTask extends Task {
    private ItemStack food;
    private long count;

    public EatItemTask(long id, Quest quest) {
        super(id, quest);
        this.food = ItemStack.EMPTY;
        this.count = 1L;
    }

    public TaskType getType() {
        return TasksRegistry.EAT_ITEM;
    }

    public long getMaxProgress() {
        return this.count;
    }

    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        NBTUtils.write(nbt, "items", this.food);
        nbt.putLong("count", this.count);
    }

    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        this.food = NBTUtils.read(nbt, "items");
        this.count = Math.max(nbt.getLong("count"), 1L);
    }

    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        FTBQuestsNetHandler.writeItemType(buffer, this.food);
        buffer.writeVarLong(this.count);
    }

    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.food = FTBQuestsNetHandler.readItemType(buffer);
        this.count = buffer.readVarLong();
    }

    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addItemStack("items", this.food, (v) -> this.food = v, ItemStack.EMPTY, true, false).setNameKey("ftbquests.task.ftbquests.items");
        config.addLong("count", this.count, (v) -> this.count = v, 1L, 1L, Long.MAX_VALUE);
    }

    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return this.count > 1L ? Component.literal(this.count + "x ").append(this.food.getHoverName()) : Component.literal("").append(this.food.getHoverName());
    }

    public List<ItemStack> getValidDisplayItems() {
        List<ItemStack> list = new ArrayList<>();
        ItemFiltersAPI.getDisplayItemStacks(this.food, list);
        return list;
    }

    @OnlyIn(Dist.CLIENT)
    public Icon getAltIcon() {
        List<Icon> icons = new ArrayList<>();

        for (ItemStack stack : this.getValidDisplayItems()) {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            Icon icon = ItemIcon.getItemIcon(copy);
            if (!icon.isEmpty()) {
                icons.add(icon);
            }
        }

        if (icons.isEmpty()) {
            return ItemIcon.getItemIcon(FTBQuestsItems.MISSING_ITEM.get());
        } else {
            return IconAnimation.fromList(icons, false);
        }
    }

    public void eat(TeamData teamData, ServerPlayer player, ItemStack foodItem) {
        if (!foodItem.isEmpty() && this.test(foodItem)) {
            teamData.addProgress(this, foodItem.getCount());
        }
    }

    public boolean test(ItemStack stack) {
        if (this.food.isEmpty()) {
            return false;
        } else {
            IItemFilter f = ItemFiltersAPI.getFilter(this.food);
            return f != null ? f.filter(this.food, stack) : this.areItemStacksEqual(this.food, stack);
        }
    }

    private boolean areItemStacksEqual(ItemStack stackA, ItemStack stackB) {
        if (stackA == stackB) {
            return true;
        } else if (stackA.getItem() == stackB.getItem()) {
            return true;
        } else {
            return false;
        }
    }
}
