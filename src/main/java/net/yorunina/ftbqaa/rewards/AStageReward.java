package net.yorunina.ftbqaa.rewards;

import com.alessandro.astages.api.AStagesUtils;
import com.alessandro.astages.api.holder.AHolder;
import com.alessandro.astages.capability.*;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AStageReward extends Reward {
    private String stage = "";
    private boolean remove = false;
    private boolean isServer = false;

    public AStageReward(long id, Quest quest) {
        super(id, quest);
        this.autoclaim = RewardAutoClaim.INVISIBLE;
    }

    public RewardType getType() {
        return AARewardTypes.ASTAGE;
    }

    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putString("stage", this.stage);
        if (this.remove) {
            nbt.putBoolean("remove", true);
        }
        if (this.isServer) {
            nbt.putBoolean("isServer", true);
        }
    }

    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        this.stage = nbt.getString("stage");
        this.remove = nbt.getBoolean("remove");
        this.isServer = nbt.getBoolean("isServer");
    }

    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(this.stage, 32767);
        buffer.writeBoolean(this.remove);
        buffer.writeBoolean(this.isServer);
    }

    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.stage = buffer.readUtf(32767);
        this.remove = buffer.readBoolean();
        this.isServer = buffer.readBoolean();
    }

    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addString("stage", this.stage, (v) -> this.stage = v, "").setNameKey("ftbquests.reward.ftbquests.gamestage");
        config.addBool("remove", this.remove, (v) -> this.remove = v, false);
        config.addBool("isServer", this.isServer, (v) -> this.isServer = v, false);
    }

    public void claim(ServerPlayer player, boolean notify) {
        if (this.isServer) {
            if (this.remove) {
                ServerStage.removeServerStage(this.stage);
            } else {
                ServerStage.addServerStage(this.stage);
            }
        } else {
            if (this.remove) {
                AStagesUtils.removeStage(AHolder.player(player), this.stage, true);
            } else {
                AStagesUtils.addStage(AHolder.player(player), this.stage, true);
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.translatable("ftbquests.reward.ftbquests.gamestage").append(": ").append(Component.literal(this.stage).withStyle(ChatFormatting.YELLOW));
    }

    public boolean ignoreRewardBlocking() {
        return true;
    }

    protected boolean isIgnoreRewardBlockingHardcoded() {
        return true;
    }
}