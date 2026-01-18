package net.yorunina.maa.compat.kubejs;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.yorunina.maa.compat.kubejs.events.KubeStructureProcessorJS;
import net.yorunina.maa.registries.ProcessorRegister;

import javax.annotation.Nullable;

import static net.yorunina.maa.compat.kubejs.Plugin.STRUCTURE_PROCESSOR;

public class KubeStructureProcessor extends StructureProcessor {
    public static final Codec<KubeStructureProcessor> CODEC = Codec.STRING.fieldOf("sub_processor").xmap(KubeStructureProcessor::new, (p_74023_) -> {
        return p_74023_.subProcessorId;
    }).codec();

    private final String subProcessorId;
    public KubeStructureProcessor(String id) {
        this.subProcessorId = id;
    }

    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos pBlockPos, BlockPos posB, StructureTemplate.StructureBlockInfo structureBlockInfo, StructureTemplate.StructureBlockInfo pBlockInfo, StructurePlaceSettings structurePlaceSettings) {
        if (pBlockInfo.state().is(Blocks.STRUCTURE_BLOCK)) {
            KubeStructureProcessorJS event = new KubeStructureProcessorJS(levelReader, pBlockPos, posB, structureBlockInfo, pBlockInfo, structurePlaceSettings);
            STRUCTURE_PROCESSOR.post(event, subProcessorId);
            return event.getBlockInfo();
        }
        return pBlockInfo;
    }

    protected StructureProcessorType<?> getType() {
        return ProcessorRegister.KUBE_PROCESS;
    }
}