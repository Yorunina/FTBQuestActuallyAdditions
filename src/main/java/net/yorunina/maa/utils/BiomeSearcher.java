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
                    if (isCancelled(searchId)) {
                        callback.accept(null);
                        return;
                    }
                    if (result != null && !result.equals(BlockPos.ZERO)) {
                        BlockPos centerPos = calculateBiomeCenter(level, result, target, searchId);
                        if (!isCancelled(searchId)) {
                            callback.accept(centerPos);
                        } else {
                            callback.accept(null);
                        }
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
        int minBuildHeight = level.getMinBuildHeight() + 1;
        int maxBuildHeight = level.getMaxBuildHeight();

        int step = 64;
        int x = centerX;
        int z = centerZ;
        int dx = step;
        int dz = 0;
        int segmentLength = 1;
        int segmentPassed = 0;
        int checkInterval = 100;

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if (i % checkInterval == 0 && (isCancelled(searchId) || shutdown)) {
                return BlockPos.ZERO;
            }

            int distance = (int) Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2));
            if (distance > maxRadius) {
                return BlockPos.ZERO;
            }

            BlockPos foundPos = checkBiomeAt(source, sampler, targetBiome, x, y, z, minBuildHeight, maxBuildHeight);
            if (foundPos != null) {
                return foundPos;
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

        return BlockPos.ZERO;
    }

    private BlockPos checkBiomeAt(BiomeSource source, Climate.Sampler sampler, ResourceLocation targetBiome, int x, int y, int z, int minBuildHeight, int maxBuildHeight) {
        int[] searchedHeights = Mth.outFromOrigin(y, minBuildHeight, maxBuildHeight, 64).toArray();
        int quartX = QuartPos.fromBlock(x);
        int quartZ = QuartPos.fromBlock(z);

        for (int testY : searchedHeights) {
            int quartY = QuartPos.fromBlock(testY);
            Holder<Biome> holder = source.getNoiseBiome(quartX, quartY, quartZ, sampler);
            if (holder.is(targetBiome)) {
                return new BlockPos(x, testY, z);
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
        
        while (biomeUp < 32 && !isCancelled(searchId) && getNoiseBiomeAtPos(source, biomeCorner.above(biomeUp), sampler).is(biome)) {
            biomeUp += 8;
        }
        
        while (biomeDown < 64 && !isCancelled(searchId) && getNoiseBiomeAtPos(source, biomeCorner.below(biomeDown), sampler).is(biome)) {
            biomeDown += 8;
        }
        
        int centerY = biomeCorner.getY() + (biomeUp - biomeDown) / 2;
        BlockPos yCentered = biomeCorner.atY(centerY);
        
        while (biomeNorth < 800 && !isCancelled(searchId) && getNoiseBiomeAtPos(source, yCentered.north(biomeNorth), sampler).is(biome)) {
            biomeNorth += 8;
        }
        
        while (biomeSouth < 800 && !isCancelled(searchId) && getNoiseBiomeAtPos(source, yCentered.south(biomeSouth), sampler).is(biome)) {
            biomeSouth += 8;
        }
        
        while (biomeEast < 800 && !isCancelled(searchId) && getNoiseBiomeAtPos(source, yCentered.east(biomeEast), sampler).is(biome)) {
            biomeEast += 8;
        }
        
        while (biomeWest < 800 && !isCancelled(searchId) && getNoiseBiomeAtPos(source, yCentered.west(biomeWest), sampler).is(biome)) {
            biomeWest += 8;
        }
        
        return yCentered.offset(biomeEast - biomeWest, 0, biomeSouth - biomeNorth);
    }

    private Holder<Biome> getNoiseBiomeAtPos(BiomeSource source, BlockPos pos, Climate.Sampler sampler){
        return source.getNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2, sampler);
    }
}