package net.yorunina.ftbqaa.tasks;

import com.alessandro.astages.event.custom.LivingEntityEatEvent;
import dev.ftb.mods.ftbquests.api.QuestFile;
import dev.ftb.mods.ftbquests.events.ClearFileCacheEvent;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.data.TeamManagerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yorunina.ftbqaa.FTBQuestActuallyAdditions;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = FTBQuestActuallyAdditions.MODID)
public class FTBQAAEventHandler {
    private static List<EatItemTask> eatItemTaskList = new ArrayList<>();

    public void init() {
        ClearFileCacheEvent.EVENT.register(this::fileCacheClear);
    }

    private void fileCacheClear(QuestFile file) {
        if (file.isServerSide()) {
            eatItemTaskList = null;
        }
    }

    @SubscribeEvent
    public static void onLivingEntityEat(LivingEntityEatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (eatItemTaskList == null) {
            eatItemTaskList = ServerQuestFile.INSTANCE.collect(EatItemTask.class);
        }
        if (eatItemTaskList.isEmpty()) return;
        Team team = TeamManagerImpl.INSTANCE.getTeamForPlayer(player).orElse(null);
        if (team == null) return;
        TeamData teamData = ServerQuestFile.INSTANCE.getOrCreateTeamData(team);
        for (EatItemTask task : eatItemTaskList) {
            if (teamData.getProgress(task) < task.getMaxProgress() && teamData.canStartTasks(task.getQuest())) {
                task.eat(teamData, player, event.getFood());
            }
        }
    }
}
