package net.yorunina.ftbqaa.rewards;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import net.yorunina.ftbqaa.FTBQuestActuallyAdditions;

public interface AARewardTypes {
    RewardType ASTAGE = RewardTypes.register(FTBQuestActuallyAdditions.id( "astages"), AStageReward::new, () -> Icons.CONTROLLER);
    static void init() {}
}
