package net.yorunina.ftbqaa.tasks;

import com.alessandro.astages.api.AStagesUtils;
import com.alessandro.astages.api.holder.AHolder;
import com.alessandro.astages.capability.ServerStage;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.*;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AStageTask extends AbstractBooleanTask {
    private String stage = "";
    private boolean isServer = false;

    public AStageTask(long id, Quest quest) {
        super(id, quest);
    }

    public TaskType getType() {
        return AATaskTypes.ASTAGE;
    }

    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putString("stage", this.stage);
        nbt.putBoolean("isServer", this.isServer);
    }

    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        this.stage = nbt.getString("stage");
        this.isServer = nbt.getBoolean("isServer");
    }

    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(this.stage, 32767);
        buffer.writeBoolean(this.isServer);
    }

    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.stage = buffer.readUtf(32767);
        this.isServer = buffer.readBoolean();
    }

    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addString("stage", this.stage, (v) -> this.stage = v, "").setNameKey("ftbquests.task.ftbquests.gamestage");
        config.addBool("isServer", this.isServer, (v) -> this.isServer = v, false);
    }

    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.translatable("ftbquests.task.ftbquests.gamestage").append(": ").append(Component.literal(this.stage).withStyle(ChatFormatting.YELLOW));
    }

    public int autoSubmitOnPlayerTick() {
        return 100;
    }

    public boolean canSubmit(TeamData teamData, ServerPlayer player) {
        if (this.isServer) {
            return ServerStage.getServerStages().contains(this.stage);
        } else {
            return AStagesUtils.hasStage(AHolder.player(player), this.stage);
        }
    }

}
