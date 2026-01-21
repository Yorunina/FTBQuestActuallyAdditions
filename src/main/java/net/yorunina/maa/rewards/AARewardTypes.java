package net.yorunina.maa.rewards;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import net.yorunina.maa.ModpackActuallyAdditions;

public interface AARewardTypes {
    RewardType ASTAGE = RewardTypes.register(ModpackActuallyAdditions.id( "astages"), AStageReward::new, () -> Icons.CONTROLLER);
    RewardType ACTIVE_WAY_STONE = RewardTypes.register(ModpackActuallyAdditions.id("active_way_stone"), ActiveWayStoneReward::new, () -> Icons.CONTROLLER);
    RewardType KUBE = RewardTypes.register(ModpackActuallyAdditions.id("kube"), KubeReward::new, () -> Icons.CONTROLLER);

    static void init() {}
}
