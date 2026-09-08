package net.yorunina.maa.compat.leaderboards;

import com.leclowndu93150.leaderboards.LeaderboardRegistry;
import com.leclowndu93150.leaderboards.Leaderboards;
import com.leclowndu93150.leaderboards.VanillaStatsRegistry;
import com.leclowndu93150.leaderboards.data.Leaderboard;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public abstract class LeaderboardTask extends Task {
    protected ResourceLocation leaderboard;

    protected LeaderboardTask(long id, Quest quest) {
        super(id, quest);
        this.leaderboard = ResourceLocation.fromNamespaceAndPath(Leaderboards.MODID, "mob_kills");
    }

    protected Leaderboard resolveLeaderboard() {
        return LeaderboardsCompat.resolve(this.leaderboard);
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putString("leaderboard", this.leaderboard.toString());
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        this.leaderboard = ResourceLocation.tryParse(nbt.getString("leaderboard"));
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeResourceLocation(this.leaderboard);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.leaderboard = buffer.readResourceLocation();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        List<ResourceLocation> list = new ArrayList<>();
        list.addAll(LeaderboardRegistry.LEADERBOARDS.keySet());
        list.addAll(VanillaStatsRegistry.VANILLA_STATS.keySet());
        config.addEnum("leaderboard", this.leaderboard, v -> this.leaderboard = v,
                NameMap.of(ResourceLocation.fromNamespaceAndPath(Leaderboards.MODID, "mob_kills"), list)
                        .nameKey(v -> "leaderboard." + v.getNamespace() + "." + v.getPath())
                        .icon(v -> ItemIcon.getItemIcon(Items.SPAWNER))
                        .create());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.translatable("leaderboard." + this.leaderboard.getNamespace() + "." + this.leaderboard.getPath());
    }

    @Override
    public int autoSubmitOnPlayerTick() {
        return 100;
    }
}
