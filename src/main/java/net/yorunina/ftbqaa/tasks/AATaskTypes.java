package net.yorunina.ftbqaa.tasks;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.yorunina.ftbqaa.FTBQuestActuallyAdditions;

public interface AATaskTypes {
    TaskType ASTAGE = TaskTypes.register(FTBQuestActuallyAdditions.id( "astages"), AStageTask::new, () -> Icons.CONTROLLER);
    static void init() {}
}
