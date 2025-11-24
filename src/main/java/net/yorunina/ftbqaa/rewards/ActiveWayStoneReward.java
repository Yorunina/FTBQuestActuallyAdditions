package net.yorunina.ftbqaa.rewards;

import com.alessandro.astages.api.AStagesUtils;
import com.alessandro.astages.api.holder.AHolder;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.blay09.mods.waystones.core.WaystoneManager;
import net.blay09.mods.waystones.core.WaystoneSyncManager;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ActiveWayStoneReward extends Reward {
    private String name = "";
    private boolean remove = false;

    public ActiveWayStoneReward(long id, Quest quest) {
        super(id, quest);
        this.autoclaim = RewardAutoClaim.INVISIBLE;
    }

    public RewardType getType() {
        return AARewardTypes.ACTIVE_WAY_STONE;
    }

    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putString("name", this.name);
        if (this.remove) {
            nbt.putBoolean("remove", true);
        }
    }

    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        this.name = nbt.getString("name");
        this.remove = nbt.getBoolean("remove");
    }

    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(this.name, 32767);
        buffer.writeBoolean(this.remove);
    }

    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.name = buffer.readUtf(32767);
        this.remove = buffer.readBoolean();
    }

    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addString("name", this.name, (v) -> this.name = v, "").setNameKey("ftbquests.reward.ftbqaa.active_way_stone");
        config.addBool("remove", this.remove, (v) -> this.remove = v, false);
    }

    public void claim(ServerPlayer player, boolean notify) {
        if (this.remove) {
            WaystoneManager waystoneManager = WaystoneManager.get(player.server);
            waystoneManager.findWaystoneByName(this.name).ifPresent(waystone -> {
                PlayerWaystoneManager.deactivateWaystone(player, waystone);
            });
        } else {
            WaystoneManager waystoneManager = WaystoneManager.get(player.server);
            waystoneManager.findWaystoneByName(this.name).ifPresent(waystone -> {
                PlayerWaystoneManager.activateWaystone(player, waystone);
            });
        }
        WaystoneSyncManager.sendActivatedWaystones(player);
    }



    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.translatable("ftbquests.reward.ftbqaa.active_way_stone").append(": ").append(Component.literal(this.name).withStyle(ChatFormatting.YELLOW));
    }

    public boolean ignoreRewardBlocking() {
        return true;
    }

    protected boolean isIgnoreRewardBlockingHardcoded() {
        return true;
    }
}