package net.yorunina.maa.events;

import com.alessandro.astages.api.event.world.LivingEntityEatEvent;
import com.tom.createores.block.DrillBlock;
import com.tom.createores.block.ExtractorBlock;
import com.tom.createores.block.entity.DrillBlockEntity;
import com.tom.createores.block.entity.ExtractorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.fml.common.Mod;
import net.yorunina.maa.ModpackActuallyAdditions;
import net.yorunina.maa.compat.kubejs.MAAEvents;
import net.yorunina.maa.compat.kubejs.MAAUtils;
import net.yorunina.maa.compat.kubejs.events.PlayerDimensionChangeEventJS;
import net.yorunina.maa.model.IDrillBlock;
import net.yorunina.maa.registry.MAAGameRules;
import net.yorunina.maa.tasks.TasksRegistry;

@Mod.EventBusSubscriber(modid = ModpackActuallyAdditions.MODID)
public class Events {
    @SubscribeEvent
    public static void onLivingEntityEat(LivingEntityEatEvent event) {
        if (event.getPlayer() == null) return;
        TasksRegistry.getInstance().onLivingEntityEat(event.getPlayer(), event.getFood());
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        BlockState blockState = event.getPlacedBlock();
        LevelAccessor pLevel = event.getLevel();
        BlockPos pPos = event.getPos();
        Entity pPlacer = event.getEntity();
        if (!(pPlacer instanceof Player)) return;

        Block block = blockState.getBlock();
        if (block instanceof DrillBlock drillBlock) {
            DrillBlockEntity entity = drillBlock.getBlockEntity(pLevel, pPos);
            if (entity != null) {
                ((IDrillBlock) entity).setOwner(pPlacer.getUUID());
            }
        }
        if (block instanceof ExtractorBlock extractorBlock) {
            ExtractorBlockEntity entity = extractorBlock.getBlockEntity(pLevel, pPos);
            if (entity != null) {
                ((IDrillBlock) entity).setOwner(pPlacer.getUUID());
            }
        }
    }


    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            MAAUtils.INSTANCE.syncEternalWinterToPlayer(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MAAGameRules.onServerLoaded(event.getServer());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerTravelToDimension(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ResourceKey<Level> fromDimension = serverPlayer.level().dimension();
            ResourceKey<Level> toDimension = event.getDimension();


            if (MAAEvents.PLAYER_DIMENSION_CHANGE.post(new PlayerDimensionChangeEventJS(serverPlayer, fromDimension, toDimension)).arch().isFalse()) {
                event.setCanceled(true);
            }
        }
    }
}