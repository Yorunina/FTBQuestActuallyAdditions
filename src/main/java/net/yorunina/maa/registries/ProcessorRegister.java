package net.yorunina.maa.registries;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.yorunina.maa.ModpackActuallyAdditions;
import net.yorunina.maa.compat.kubejs.KubeStructureProcessor;
import org.jetbrains.annotations.NotNull;

public class ProcessorRegister {
    public static final StructureProcessorType<KubeStructureProcessor> KUBE_PROCESS = register(ModpackActuallyAdditions.id("kube_process"), KubeStructureProcessor.CODEC);

    private static <P extends StructureProcessor> StructureProcessorType<P> register(ResourceLocation id, @NotNull Codec<P> codec) {
        return Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, id, () -> codec);
    }

    public static void registerProcessors() {
    }
}
