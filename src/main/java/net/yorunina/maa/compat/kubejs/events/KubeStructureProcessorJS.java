package net.yorunina.maa.compat.kubejs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class KubeStructureProcessorJS extends EventJS {
    private final LevelReader levelReader;
    private final BlockPos blockPos;
    private  StructureBlockInfo blockInfo;
    private final StructurePlaceSettings structurePlaceSettings;


    public KubeStructureProcessorJS(LevelReader levelReader, BlockPos blockPos, BlockPos posB, StructureBlockInfo structureBlockInfo, StructureBlockInfo blockInfo, StructurePlaceSettings structurePlaceSettings) {
        super();
        this.levelReader = levelReader;
        this.blockPos = blockPos;
        this.blockInfo = blockInfo;
        this.structurePlaceSettings = structurePlaceSettings;
    }

    public LevelReader getLevelReader() {
        return levelReader;
    }
    public BlockPos getBlockPos() {
        return blockPos;
    }
    public StructureBlockInfo getBlockInfo() {
        return blockInfo;
    }
    public void setBlockInfo(StructureBlockInfo blockInfo) {
        this.blockInfo = blockInfo;
    }
    public StructurePlaceSettings getStructurePlaceSettings() {
        return structurePlaceSettings;
    }
}
