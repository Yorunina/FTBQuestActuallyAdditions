package net.yorunina.maa.rewards;

import com.alessandro.astages.api.AStagesUtils;
import com.alessandro.astages.api.holder.AHolder;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.events.CustomRewardEvent;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.CustomReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbxmodcompat.ftbquests.kubejs.CustomRewardEventJS;
import dev.ftb.mods.ftbxmodcompat.ftbquests.kubejs.FTBQuestsKubeJSEvents;
import dev.ftb.mods.ftbxmodcompat.ftbquests.kubejs.KubeJSIntegration;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class KubeReward extends CustomReward {
    private String kubeId = "";

    public KubeReward(long id, Quest quest) {
        super(id, quest);
    }

    public RewardType getType() {
        return AARewardTypes.KUBE;
    }

    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putString("kubeId", this.kubeId);
    }

    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        this.kubeId = nbt.getString("kubeId");
    }

    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(this.kubeId, 32767);
    }

    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.kubeId = buffer.readUtf(32767);
    }

    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addString("kubeId", this.kubeId, (v) -> this.kubeId = v, "").setNameKey("ftbquests.reward.maa.kubeId");
    }

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        FTBQuestsKubeJSEvents.CUSTOM_REWARD.post( ScriptType.SERVER, this.kubeId, new CustomRewardEventJS(new CustomRewardEvent(this, player, false))).arch();
    }

    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.translatable("ftbquests.reward.maa.kubeReward").append(": ").append(Component.literal(this.kubeId).withStyle(ChatFormatting.YELLOW));
    }

    public boolean ignoreRewardBlocking() {
        return true;
    }

    protected boolean isIgnoreRewardBlockingHardcoded() {
        return true;
    }
}
