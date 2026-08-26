package net.yorunina.maa.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yorunina.maa.ModpackActuallyAdditions;

import static net.yorunina.maa.ModpackActuallyAdditions.MODID;


@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SmartSpawnHandler {
    private static final int WATER_CHECK_RADIUS = 10;

    private static final int MAX_SEARCH_RADIUS = 1024;

    private static final int MIN_SAMPLE_SPACING = 32;

    private static final int MAX_SAMPLE_POINTS = 56;

    private static final int LOADED_SEARCH_RADIUS = 128;

    private static final int MAX_FORCED_CHUNKS = 32;

    private static final int[][] DIRS = {
            {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}
    };

    private SmartSpawnHandler() {
    }

    /**
     * 用 ServerStartedEvent 时出生点与出生区块已就绪，可安全读取/移动出生点。
     */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }

        BlockPos spawn = overworld.getSharedSpawnPos();
        if (!isNearWater(overworld, spawn)) {
            return;
        }

        BlockPos safe = findSafeSpawn(overworld, spawn);
        if (safe == null) {
            ModpackActuallyAdditions.LOGGER.warn("[MAA] No dry land near spawn {}", spawn);
            return;
        }

        overworld.setDefaultSpawnPos(safe, 0.0F);
        ModpackActuallyAdditions.LOGGER.info("[MAA] Spawn moved {} -> {}", spawn, safe);
    }

    /**
     * 分两阶段搜索安全位置：
     * 阶段1只在已加载的出生区块范围内采样，不触发任何区块生成；
     * 找不到才进入阶段2，在强生预算内向外扩张。
     */
    private static BlockPos findSafeSpawn(ServerLevel level, BlockPos center) {
        int[] counters = {0, 0}; // [已采样数, 已强制生成数]
        int cx = center.getX();
        int cz = center.getZ();

        BlockPos near = sampleRings(level, cx, cz, MIN_SAMPLE_SPACING, LOADED_SEARCH_RADIUS, false, counters);
        if (near != null) {
            return near;
        }
        return sampleRings(level, cx, cz, LOADED_SEARCH_RADIUS * 2, MAX_SEARCH_RADIUS, true, counters);
    }

    private static BlockPos sampleRings(ServerLevel level, int cx, int cz, int from, int to,
                                        boolean allowForce, int[] counters) {
        for (int radius = from; radius <= to; radius *= 2) {
            for (int[] dir : DIRS) {
                if (counters[0]++ >= MAX_SAMPLE_POINTS) {
                    return null;
                }
                BlockPos candidate = tryCandidate(level,
                        cx + dir[0] * radius, cz + dir[1] * radius, allowForce, counters);
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }


    private static BlockPos tryCandidate(ServerLevel level, int x, int z, boolean allowForce, int[] counters) {
        if (!level.isLoaded(new BlockPos(x, 0, z))) {
            if (!allowForce || counters[1] >= MAX_FORCED_CHUNKS) {
                return null;
            }
            counters[1]++;
        }
        BlockPos pos = findStandingPos(level, x, z);
        return pos != null && isSafeSpawn(level, pos) ? pos : null;
    }


    private static boolean isNearWater(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cz = center.getZ();
        for (int dx = -WATER_CHECK_RADIUS; dx <= WATER_CHECK_RADIUS; dx++) {
            for (int dz = -WATER_CHECK_RADIUS; dz <= WATER_CHECK_RADIUS; dz++) {
                int x = cx + dx;
                int z = cz + dz;
                if (level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z)
                        > level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z)) {
                    return true;
                }
            }
        }
        return false;
    }


    private static BlockPos findStandingPos(ServerLevel level, int x, int z) {
        int ground = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
        if (ground < level.getMinBuildHeight() + 1) {
            return null;
        }
        BlockState state = level.getBlockState(new BlockPos(x, ground, z));
        return state.isSolid() && !isLiquid(state) ? new BlockPos(x, ground + 1, z) : null;
    }


    private static boolean isSafeSpawn(ServerLevel level, BlockPos pos) {
        if (pos.getY() < level.getMinBuildHeight() + 1 || pos.getY() >= level.getMaxBuildHeight()) {
            return false;
        }
        BlockState ground = level.getBlockState(pos.below());
        if (!ground.isSolid() || isLiquid(ground)) {
            return false;
        }
        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());
        return !isLiquid(feet) && !isLiquid(head)
                && (feet.isAir() || feet.canBeReplaced())
                && (head.isAir() || head.canBeReplaced());
    }


    private static boolean isLiquid(BlockState state) {
        return !state.getFluidState().isEmpty();
    }
}