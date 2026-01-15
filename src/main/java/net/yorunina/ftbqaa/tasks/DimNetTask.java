package net.yorunina.ftbqaa.tasks;

import com.wintercogs.beyonddimensions.Api.DataBase.DimensionsNet;
import com.wintercogs.beyonddimensions.Api.DataBase.Storage.UnifiedStorage;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.AbstractBooleanTask;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DimNetTask extends AbstractBooleanTask {
    private long slotCapacity = 0;
    private int slotMaxSize = 0;

    public DimNetTask(long id, Quest quest) {
        super(id, quest);
    }

    public TaskType getType() {
        return TasksRegistry.DIM_NET;
    }

    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putLong("slotCapacity", this.slotCapacity);
        nbt.putInt("slotMaxSize", this.slotMaxSize);
    }

    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        this.slotCapacity = nbt.getLong("slotCapacity");
        this.slotMaxSize = nbt.getInt("slotMaxSize");
    }

    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeLong(this.slotCapacity);
        buffer.writeInt(this.slotMaxSize);
    }

    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.slotCapacity = buffer.readLong();
        this.slotMaxSize = buffer.readInt();
    }

    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addLong("slotCapacity", this.slotCapacity, (v) -> this.slotCapacity = v, 0, Long.MIN_VALUE, Long.MAX_VALUE);
        config.addInt("slotMaxSize", this.slotMaxSize, (v) -> this.slotMaxSize = v, 0,  Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.translatable("ftbquests.task.ftbqaa.dim_net.title",
                Component.literal(String.valueOf(this.slotCapacity)).withStyle(ChatFormatting.YELLOW),
                Component.literal(String.valueOf(this.slotMaxSize)).withStyle(ChatFormatting.YELLOW));
    }

    public int autoSubmitOnPlayerTick() {
        return 100;
    }

    public boolean canSubmit(TeamData teamData, ServerPlayer player) {
        DimensionsNet playerNet = DimensionsNet.getNetFromPlayer(player);
        if (playerNet == null || playerNet.deleted) {
            return false;
        }
        UnifiedStorage storage = playerNet.getUnifiedStorage();
        return storage.slotCapacity >= this.slotCapacity && storage.slotMaxSize >= this.slotMaxSize;
    }
}
