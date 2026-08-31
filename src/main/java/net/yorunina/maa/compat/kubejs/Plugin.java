package net.yorunina.maa.compat.kubejs;

import dev.ftb.mods.ftbquests.api.QuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import net.mehvahdjukaar.amendments.common.recipe.FluidAndItemCraftResult;
import net.minecraft.stats.StatFormatter;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.unusual.block_factorys_bosses.capability.entity.RollCap;
import net.yorunina.maa.client.RenderHelper;
import net.yorunina.maa.client.vfx.PunchVFXManager;
import net.yorunina.maa.registry.MAAStats;
import net.yorunina.maa.utils.CompactMachineUtil;
import net.yorunina.maa.utils.MobBattleUtil;

import java.util.Comparator;

import static net.yorunina.maa.compat.kubejs.MAAEvents.MAA_GROUP;

public class Plugin extends KubeJSPlugin {
    @Override
    public void registerEvents() {
        MAA_GROUP.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("MAAUtils", MAAUtils.INSTANCE);
        event.add("FluidAndItemCraftResult", FluidAndItemCraftResult.class);
        event.add("ClickAction", ClickAction.class);
        event.add("TeamData", TeamData.class);
        event.add("QuestFile", QuestFile.class);
        event.add("Comparator", Comparator.class);
        event.add("StatFormatter", StatFormatter.class);
        event.add("MAAStats", MAAStats.class);
        event.add("EnchantmentHelper", EnchantmentHelper.class);
        event.add("CompactMachineUtil", CompactMachineUtil.class);
        event.add("MobBattleUtil", MobBattleUtil.class);
        event.add("RollCap", RollCap.class);
        if (event.getType().isClient()) {
            event.add("RenderHelper", RenderHelper.INSTANCE);
            event.add("ShockwaveStyle", PunchVFXManager.ShockwaveStyle.class);
        }
    }
}
