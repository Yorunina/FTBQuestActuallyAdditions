package net.yorunina.maa.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BiomeSearcher {
    private static final int SEARCH_STEP = 64;
    private static final int HEIGHT_STEP = 64;
    private static final int CENTER_STEP = 8;
    private static final int CANCELLATION_CHECK_INTERVAL = 64;
    private static final int MAX_VERTICAL_UP = 32;
    private static final int MAX_VERTICAL_DOWN = 64;
    private static final int MAX_HORIZONTAL_DISTANCE = 800;

    private static volatile ExecutorService EXECUTORS = Executors.newFixedThreadPool(4);
    private static final Set<UUID> CANCELLED = ConcurrentHashMap.newKeySet();
    private static volatile boolean shutdown = false;

    private static ExecutorService getExecutor() {
        if (EXECUTORS.isShutdown() || EXECUTORS.isTerminated()) {
            synchronized (BiomeSearcher.class) {
                if (EXECUTORS.isShutdown() || EXECUTORS.isTerminated()) {
                    EXECUTORS = Executors.newFixedThreadPool(4);
                    shutdown = false;
                    CANCELLED.clear();
                }
            }
        }
        return EXECUTORS;
    }

    public static void shutdown() {
        shutdown = true;
        EXECUTORS.shutdown();
        try {
            if (!EXECUTORS.awaitTermination(10, TimeUnit.SECONDS)) {
                EXECUTORS.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTORS.shutdownNow();
            Thread.currentThread().interrupt();
        }
        CANCELLED.clear();
    }

    public static void cancelSearch(UUID searchId) {
        if (searchId != null) {
            CANCELLED.add(searchId);
        }
    }

    public static boolean isCancelled(UUID searchId) {
        return CANCELLED.contains(searchId);
    }

    public UUID searchAsync(@NotNull ServerLevel level, @NotNull ResourceLocation target, @NotNull BlockPos center, int maxRadius, @NotNull Consumer<BlockPos> callback) {
        if (maxRadius <= 0) {
            callback.accept(null);
            return null;
        }

        if (shutdown) {
            callback.accept(null);
            return null;
        }

        UUID searchId = UUID.randomUUID();

        try {
            getExecutor().submit(() -> {
                if (shutdown) {
                    callback.accept(null);
                    return;
                }

                try {
                    BlockPos result = searchIterative(level, target, center, maxRadius, searchId);
                    if (result == null || isCancelled(searchId)) {
                        callback.accept(null);
                        return;
                    }
                    BlockPos centerPos = calculateBiomeCenter(level, result, target, searchId);
                    if (!isCancelled(searchId)) {
                        callback.accept(centerPos);
                    } else {
                        callback.accept(null);
                    }
                } catch (Exception e) {
                    callback.accept(null);
                } finally {
                    CANCELLED.remove(searchId);
                }
            });
        } catch (RejectedExecutionException e) {
            CANCELLED.remove(searchId);
            callback.accept(null);
            return null;
        }

        return searchId;
    }


    private BlockPos searchIterative(ServerLevel level, ResourceLocation targetBiome, BlockPos center, int maxRadius, UUID searchId) {
        int centerX = center.getX();
        int centerZ = center.getZ();
        int y = center.getY();

        ServerChunkCache cache = level.getChunkSource();
        BiomeSource source = cache.getGenerator().getBiomeSource();
        Climate.Sampler sampler = cache.randomState().sampler();
        if (source.possibleBiomes().stream().noneMatch(holder -> holder.is(targetBiome))) {
            return null;
        }

        int minBuildHeight = level.getMinBuildHeight() + 1;
        int maxBuildHeight = level.getMaxBuildHeight();
        int[] searchedHeights = Mth.outFromOrigin(y, minBuildHeight, maxBuildHeight, HEIGHT_STEP).toArray();
        int[] searchedQuartYs = new int[searchedHeights.length];
        for (int i = 0; i < searchedHeights.length; i++) {
            searchedQuartYs[i] = QuartPos.fromBlock(searchedHeights[i]);
        }

        long x = centerX;
        long z = centerZ;
        int dx = SEARCH_STEP;
        int dz = 0;
        int segmentLength = 1;
        int segmentPassed = 0;
        long radiusSquared = (long) maxRadius * maxRadius;

        for (int samples = 0; ; samples++) {
            if ((samples & (CANCELLATION_CHECK_INTERVAL - 1)) == 0
                    && (isCancelled(searchId) || shutdown)) {
                return null;
            }

            long deltaX = x - centerX;
            long deltaZ = z - centerZ;
            if (x < Integer.MIN_VALUE || x > Integer.MAX_VALUE
                    || z < Integer.MIN_VALUE || z > Integer.MAX_VALUE) {
                return null;
            }

            long distanceSquared = deltaX * deltaX + deltaZ * deltaZ;
            if (distanceSquared <= radiusSquared) {
                BlockPos foundPos = checkBiomeAt(source, sampler, targetBiome,
                        (int) x, (int) z, searchedHeights, searchedQuartYs);
                if (foundPos != null) {
                    return foundPos;
                }
            } else if (Math.max(Math.abs(deltaX), Math.abs(deltaZ)) > maxRadius) {
                // The spiral can temporarily leave the circle and enter it
                // again (for example, (64, 64) followed by (0, 64)). Only
                // stop once the whole next square ring is outside the radius.
                return null;
            }

            x += dx;
            z += dz;
            segmentPassed++;

            if (segmentPassed == segmentLength) {
                segmentPassed = 0;
                int temp = dx;
                dx = -dz;
                dz = temp;

                if (dz == 0) {
                    segmentLength++;
                }
            }
        }
    }

    private BlockPos checkBiomeAt(BiomeSource source, Climate.Sampler sampler,
                                  ResourceLocation targetBiome, int x, int z,
                                  int[] searchedHeights, int[] searchedQuartYs) {
        int quartX = QuartPos.fromBlock(x);
        int quartZ = QuartPos.fromBlock(z);

        for (int i = 0; i < searchedHeights.length; i++) {
            Holder<Biome> holder = source.getNoiseBiome(quartX, searchedQuartYs[i], quartZ, sampler);
            if (holder.is(targetBiome)) {
                return new BlockPos(x, searchedHeights[i], z);
            }
        }
        return null;
    }

    private BlockPos calculateBiomeCenter(ServerLevel worldIn, BlockPos biomeCorner, ResourceLocation biome, UUID searchId) {
        ServerChunkCache cache = worldIn.getChunkSource();
        BiomeSource source = cache.getGenerator().getBiomeSource();
        Climate.Sampler sampler = cache.randomState().sampler();
        
        int biomeNorth = 0;
        int biomeSouth = 0;
        int biomeEast = 0;
        int biomeWest = 0;
        int biomeUp = 0;
        int biomeDown = 0;
        
        while (biomeUp < MAX_VERTICAL_UP && !isCancelled(searchId)
                && isBiomeAt(source, biomeCorner.getX(), biomeCorner.getY() + biomeUp,
                biomeCorner.getZ(), sampler, biome)) {
            biomeUp += CENTER_STEP;
        }
        
        while (biomeDown < MAX_VERTICAL_DOWN && !isCancelled(searchId)
                && isBiomeAt(source, biomeCorner.getX(), biomeCorner.getY() - biomeDown,
                biomeCorner.getZ(), sampler, biome)) {
            biomeDown += CENTER_STEP;
        }
        
        int centerY = biomeCorner.getY() + (biomeUp - biomeDown) / 2;
        int centerX = biomeCorner.getX();
        int centerZ = biomeCorner.getZ();
        
        while (biomeNorth < MAX_HORIZONTAL_DISTANCE && !isCancelled(searchId)
                && isBiomeAt(source, centerX, centerY, centerZ - biomeNorth, sampler, biome)) {
            biomeNorth += CENTER_STEP;
        }
        
        while (biomeSouth < MAX_HORIZONTAL_DISTANCE && !isCancelled(searchId)
                && isBiomeAt(source, centerX, centerY, centerZ + biomeSouth, sampler, biome)) {
            biomeSouth += CENTER_STEP;
        }
        
        while (biomeEast < MAX_HORIZONTAL_DISTANCE && !isCancelled(searchId)
                && isBiomeAt(source, centerX + biomeEast, centerY, centerZ, sampler, biome)) {
            biomeEast += CENTER_STEP;
        }
        
        while (biomeWest < MAX_HORIZONTAL_DISTANCE && !isCancelled(searchId)
                && isBiomeAt(source, centerX - biomeWest, centerY, centerZ, sampler, biome)) {
            biomeWest += CENTER_STEP;
        }
        
        return new BlockPos(centerX + biomeEast - biomeWest, centerY, centerZ + biomeSouth - biomeNorth);
    }

    private boolean isBiomeAt(BiomeSource source, int x, int y, int z,
                              Climate.Sampler sampler, ResourceLocation targetBiome) {
        return source.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y),
                QuartPos.fromBlock(z), sampler).is(targetBiome);
    }
}
