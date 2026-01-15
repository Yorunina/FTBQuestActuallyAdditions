package net.yorunina.ftbqaa.tasks;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.api.QuestFile;
import dev.ftb.mods.ftbquests.events.ClearFileCacheEvent;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.data.TeamManagerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.yorunina.ftbqaa.FTBQuestActuallyAdditions;

import java.util.List;


public class TasksRegistry {
    public static final TaskType ASTAGES = TaskTypes.register(FTBQuestActuallyAdditions.id( "astages"), AStageTask::new, () -> Icons.CONTROLLER);
    public static final TaskType DIM_NET = TaskTypes.register(FTBQuestActuallyAdditions.id("dim_net"), DimNetTask::new, () -> Icons.CONTROLLER);
    public static final TaskType EAT_ITEM = TaskTypes.register(FTBQuestActuallyAdditions.id("eat_item"), EatItemTask::new, () -> Icons.CONTROLLER);

    private static TasksRegistry INSTANCE;

    private List<EatItemTask> eatItemTaskList = null;

    public TasksRegistry() {}

    public void init() {
        ClearFileCacheEvent.EVENT.register(this::fileCacheClear);
    }

    private void fileCacheClear(QuestFile file) {
        if (file.isServerSide()) {
            this.eatItemTaskList = null;
        }
    }

    public static TasksRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TasksRegistry();
        }
        return INSTANCE;
    }

    public void onLivingEntityEat(ServerPlayer player, ItemStack food) {
        if (this.eatItemTaskList == null) {
            this.eatItemTaskList = ServerQuestFile.INSTANCE.collect(EatItemTask.class);
        }
        if (this.eatItemTaskList.isEmpty()) return;
        Team team = TeamManagerImpl.INSTANCE.getTeamForPlayer(player).orElse(null);
        if (team == null) return;
        TeamData teamData = ServerQuestFile.INSTANCE.getOrCreateTeamData(team);
        for (EatItemTask task : this.eatItemTaskList) {
            if (teamData.getProgress(task) < task.getMaxProgress() && teamData.canStartTasks(task.getQuest())) {
                task.eat(teamData, player, food);
            }
        }
    }
}
