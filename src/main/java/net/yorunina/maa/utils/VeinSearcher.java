package net.yorunina.maa.utils;

import com.tom.createores.OreVeinGenerator;
import com.tom.createores.util.RandomSpreadGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class VeinSearcher {
    private static volatile ExecutorService EXECUTORS = Executors.newFixedThreadPool(4);
    private static final Set<UUID> CANCELLED = ConcurrentHashMap.newKeySet();
    private static volatile boolean shutdown = false;

    private static ExecutorService getExecutor() {
        if (EXECUTORS.isShutdown() || EXECUTORS.isTerminated()) {
            synchronized (VeinSearcher.class) {
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

    public UUID searchAsync(@NotNull ServerLevel level, @NotNull ResourceLocation targetVein, @NotNull BlockPos center, int maxRadius, @NotNull Consumer<BlockPos> callback) {
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
                    BlockPos result = searchIterative(level, targetVein, center, maxRadius, searchId);
                    if (isCancelled(searchId)) {
                        callback.accept(null);
                        return;
                    }
                    callback.accept(result);
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

    private BlockPos searchIterative(ServerLevel level, ResourceLocation targetVein, BlockPos center, int maxRadius, UUID searchId) {
        try {
            if (isCancelled(searchId) || shutdown) {
                return null;
            }

            RandomSpreadGenerator picker = OreVeinGenerator.getPicker(level);
            if (picker == null) {
                return null;
            }

            BlockPos foundPos = picker.locate(targetVein, center, level, maxRadius);
            
            if (isCancelled(searchId)) {
                return null;
            }

            return foundPos;
        } catch (Exception e) {
            return null;
        }
    }
}