package net.yorunina.maa.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.Extra;
import net.yorunina.maa.compat.kubejs.events.KubeStructureProcessorJS;

public class Plugin extends KubeJSPlugin {

    public static EventGroup MAAGROUP = EventGroup.of("MAAEvents");

    public static EventHandler STRUCTURE_PROCESSOR = MAAGROUP
            .server("structureBlockProcessor", () -> KubeStructureProcessorJS.class).extra(Extra.STRING);


    @Override
    public void registerEvents() {
        MAAGROUP.register();
    }

}
