package net.yorunina.ftbqaa.tasks;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.yorunina.ftbqaa.FTBQuestActuallyAdditions;

public interface AATaskTypes {
    TaskType ASTAGES = TaskTypes.register(FTBQuestActuallyAdditions.id( "astages"), AStageTask::new, () -> Icons.CONTROLLER);
    TaskType DIM_NET = TaskTypes.register(FTBQuestActuallyAdditions.id("dim_net"), DimNetTask::new, () -> Icons.CONTROLLER);
    TaskType EAT_ITEM = TaskTypes.register(FTBQuestActuallyAdditions.id("eat_item"), EatItemTask::new, () -> Icons.CONTROLLER);
    static void init() {

    }
}
