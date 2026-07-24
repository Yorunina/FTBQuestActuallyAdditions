package net.yorunina.maa.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.Extra;
import net.yorunina.maa.compat.kubejs.events.*;

public class MAAEvents {
    public static EventGroup MAA_GROUP = EventGroup.of("MAAEvents");
    public static EventHandler TEXT_INPUT_TASK_SUBMIT = MAA_GROUP
            .server("textInputTaskSubmit", () -> TextInputTaskSubmitJS.class)
            .extra(Extra.STRING);

    public static EventHandler CAULDRON_CRAFT_EVENT = MAA_GROUP
            .server("cauldronCraft", () -> CauldronCraftEventJS.class);
    public static EventHandler FTB_QUEST_CHECK_REPEATABLE = MAA_GROUP
            .server("ftbQuestCheckRepeatable", () -> FTBQuestCheckRepeatableJS.class).hasResult().extra(Extra.STRING);
    public static EventHandler FTB_CUSTOM_ITEM_FILTER = MAA_GROUP
            .server("ftbCustomItemFilter", () -> FTBCustomItemFilterJS.class).extra(Extra.STRING);
    public static EventHandler VILLAGER_UPDATE_SPECIAL_PRICES = MAA_GROUP
            .server("villagerUpdateSpecialPrices", () -> VillagerUpdateSpecialPrices.class);
    public static EventHandler PLAYER_DIMENSION_CHANGE = MAA_GROUP
            .server("playerDimensionChange", () -> PlayerDimensionChangeEventJS.class).hasResult();
    public static EventHandler PLAYER_ROLL = MAA_GROUP
            .server("playerRoll", () -> RollEventJS.class);
    public static EventHandler FISHING_RETRIEVE = MAA_GROUP
            .server("fishingRetrieve", () -> FishingRetrieveEventJS.class).hasResult();
}