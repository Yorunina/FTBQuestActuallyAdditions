package net.yorunina.ftbqaa.events;

import com.alessandro.astages.event.custom.LivingEntityEatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yorunina.ftbqaa.FTBQuestActuallyAdditions;
import net.yorunina.ftbqaa.tasks.TasksRegistry;

@Mod.EventBusSubscriber(modid = FTBQuestActuallyAdditions.MODID)
public class Events {
    @SubscribeEvent
    public static void onLivingEntityEat(LivingEntityEatEvent event) {
        TasksRegistry.getInstance().onLivingEntityEat(event.getPlayer(), event.getFood());
    }
}
