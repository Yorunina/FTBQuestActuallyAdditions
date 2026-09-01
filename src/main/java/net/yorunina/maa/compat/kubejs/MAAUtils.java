package net.yorunina.maa.compat.kubejs;


import com.github.L_Ender.cataclysm.capabilities.TidalTentacleCapability;
import com.github.L_Ender.cataclysm.entity.projectile.Tidal_Tentacle_Entity;
import com.github.L_Ender.cataclysm.entity.util.TidalTentacleUtil;
import com.github.L_Ender.cataclysm.init.ModCapabilities;
import com.github.L_Ender.cataclysm.init.ModEntities;
import com.mojang.datafixers.util.Function3;
import com.simibubi.create.content.kinetics.saw.TreeCutter;
import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.net.ObjectCompletedMessage;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.ProgressChange;

import net.createmod.catnip.math.VecHelper;
import net.mehvahdjukaar.dummmmmmy.common.CritRecord;
import net.mehvahdjukaar.dummmmmmy.network.ClientBoundDamageNumberMessage;
import net.mehvahdjukaar.dummmmmmy.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yorunina.maa.mixin.accessor.DrinkBlockItemAccessor;
import net.yorunina.maa.networks.SyncEternalWinterMessage;
import net.yorunina.maa.networks.SyncRepeatTaskCompletedMessage;
import net.yorunina.maa.tasks.KubeTask;
import net.yorunina.maa.tasks.TasksRegistry;
import net.yorunina.maa.utils.BiomeSearcher;
import net.yorunina.maa.utils.VeinSearcher;

import java.util.*;
import java.util.function.Consumer;


public class MAAUtils {
    public static final MAAUtils INSTANCE = new MAAUtils();
    public boolean eternalWinterEnabled = false;
    public float globalTemperature = -10;
    public boolean noFireRender = false;

    public static final BiomeSearcher BIOME_SEARCHER = new BiomeSearcher();
    public static final VeinSearcher VEIN_SEARCHER = new VeinSearcher();

    private MAAUtils() {
    }

    public void resetInstance() {
        eternalWinterEnabled = false;
        globalTemperature = -10;
        noFireRender = false;
    }

    public void resetPlayerTaskProgress(Player player) {
        if (player == null) return;
        TeamData teamData = ServerQuestFile.INSTANCE.getOrCreateTeamData(player);
        ProgressChange change = new ProgressChange(ServerQuestFile.INSTANCE, teamData.getFile(), player.getUUID());
        change.setReset(true);
        change.maybeForceProgress(teamData.getTeamId());
    }

    public TeamData getPlayerTeamData(ServerPlayer player) {
        return ServerQuestFile.INSTANCE.getOrCreateTeamData(player);
    }

    public void resetServerTaskProgress(MinecraftServer server) {
        ServerQuestFile.INSTANCE.getAllTeamData().forEach(teamData -> {
            ProgressChange change = new ProgressChange(ServerQuestFile.INSTANCE, teamData.getFile(), teamData.getTeamId());
            change.setReset(true);
            change.maybeForceProgress(teamData.getTeamId());
        });
    }

    public void onKubeTaskFinish(String taskId, ServerPlayer player, Function3<KubeTask, ServerPlayer, TeamData, Void> consumer) {
        TasksRegistry.getInstance().onKubeTaskFinish(Collections.singletonList(taskId), player, consumer);
    }

    public void onKubeTasksFinish(List<String> taskIds, ServerPlayer player, Function3<KubeTask, ServerPlayer, TeamData, Void> consumer) {
        TasksRegistry.getInstance().onKubeTaskFinish(taskIds, player, consumer);
    }

    public void setNoFireRender(boolean value) {
        this.noFireRender = value;
    }


    public void setGlobalTemperature(float temperature) {
        globalTemperature = temperature;
    }

    public float getGlobalTemperature() {
        return globalTemperature;
    }

    public void setEternalWinterEnabled(MinecraftServer server, boolean enabled) {
        eternalWinterEnabled = enabled;
        new SyncEternalWinterMessage(enabled).sendToAll(server);
    }

    public void syncEternalWinterToPlayer(ServerPlayer player) {
        new SyncEternalWinterMessage(eternalWinterEnabled).sendTo(player);
    }

    public boolean shouldSnowContinuously() {
        return eternalWinterEnabled;
    }

    public String getChapterIdString(Chapter chapter) {
        return Long.toString(chapter.getId(), 16);
    }

    public void setTeamTaskCompleted(TeamData teamData, String taskId) {
        teamData.setCompleted(Long.parseLong(taskId, 16), new Date());
    }

    public Task getTaskByTeamData(TeamData teamData, String taskId) {
        return teamData.getFile().getTask(Long.parseLong(taskId, 16));
    }


    public int repairPlayerItems(Player player, int remainingExp, int initialValue) {
        Map.Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(Enchantments.MENDING, player, ItemStack::isDamaged);
        if (entry != null) {
            ItemStack itemstack = entry.getValue();
            int i = Math.min((int) (initialValue * itemstack.getXpRepairRatio()), itemstack.getDamageValue());
            itemstack.setDamageValue(itemstack.getDamageValue() - i);
            int j = remainingExp - i / 2;
            return j > 0 ? this.repairPlayerItems(player, j, initialValue) : 0;
        } else {
            return remainingExp;
        }
    }

    public void sendClientRepeatTaskCompleted(TeamData teamData, String taskId) {
        new ObjectCompletedMessage(teamData.getTeamId(), Long.parseLong(taskId, 16)).sendTo(teamData.getOnlineMembers());
    }

    public void sendClientRepeatTaskClaimed(TeamData teamData, UUID playerId, String rewardId) {
        new SyncRepeatTaskCompletedMessage(teamData.getTeamId(), playerId, Long.parseLong(rewardId, 16)).sendTo(teamData.getOnlineMembers());
    }

    public Optional<HolderSet<Structure>> getStructureHolderSet(MinecraftServer server, ResourceLocation structureId) {
        return server.registryAccess().registryOrThrow(Registries.STRUCTURE).getHolder(ResourceKey.create(Registries.STRUCTURE, structureId)).map(HolderSet::direct);
    }

    public void setChapterCompleted(TeamData teamData, Quest quest) {
        Collection<ServerPlayer> onlineMembers = teamData.getOnlineMembers();
        Collection<ServerPlayer> notifiedPlayers;
        if (!quest.getChapter().isAlwaysInvisible() && QuestObjectBase.shouldSendNotifications()) {
            notifiedPlayers = onlineMembers;
        } else {
            notifiedPlayers = List.of();
        }
        QuestProgressEventData<Quest> progressEvent = new QuestProgressEventData<>(new Date(), teamData, quest, onlineMembers, notifiedPlayers);
        if (quest.getChapter() != null) {
            quest.getChapter().onCompleted(progressEvent);
        }
    }

    public UUID searchBiomeAsync(ServerLevel level, ResourceLocation targetBiome, BlockPos center, int maxRadius, Consumer<BlockPos> callback) {
        return BIOME_SEARCHER.searchAsync(level, targetBiome, center, maxRadius, callback);
    }

    public UUID searchVeinAsync(ServerLevel level, ResourceLocation targetVein, BlockPos center, int maxRadius, Consumer<BlockPos> callback) {
        return VEIN_SEARCHER.searchAsync(level, targetVein, center, maxRadius, callback);
    }

    public void shutdownAllSearchers() {
        BiomeSearcher.shutdown();
        VeinSearcher.shutdown();
    }

    public void setBiomeInArea(ServerLevel level, AABB area, ResourceLocation biomeId) {
        if (level == null || area == null || biomeId == null) {
            return;
        }

        ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, biomeId);
        Holder<Biome> biomeHolder = level.registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getHolder(biomeKey)
                .orElse(null);

        if (biomeHolder == null) {
            return;
        }

        int minX = (int) area.minX;
        int minY = (int) area.minY;
        int minZ = (int) area.minZ;
        int maxX = (int) area.maxX;
        int maxY = (int) area.maxY;
        int maxZ = (int) area.maxZ;

        List<ChunkAccess> affectedChunks = new ArrayList<>();
        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                LevelChunk chunk = level.getChunk(cx, cz);
                affectedChunks.add(chunk);

                int chunkMinX = cx << 4;
                int chunkMinZ = cz << 4;
                int startX = Math.max(minX, chunkMinX);
                int endX = Math.min(maxX, chunkMinX + 15);
                int startZ = Math.max(minZ, chunkMinZ);
                int endZ = Math.min(maxZ, chunkMinZ + 15);

                for (int y = minY; y <= maxY; y++) {
                    if (y < level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) continue;
                    int sectionIndex = chunk.getSectionIndex(y);
                    LevelChunkSection section = chunk.getSection(sectionIndex);
                    if (section == null) continue;

                    for (int x = startX; x <= endX; x++) {
                        for (int z = startZ; z <= endZ; z++) {
                            setBiomeInSection(section, (x & 15) >> 2, (y & 15) >> 2, (z & 15) >> 2, biomeHolder);
                        }
                    }
                }
            }
        }

        level.getChunkSource().chunkMap.resendBiomesForChunks(affectedChunks);
    }

    @SuppressWarnings("unchecked")
    private void setBiomeInSection(LevelChunkSection section, int x, int y, int z, Holder<Biome> biomeHolder) {
        PalettedContainerRO<Holder<Biome>> biomes = section.getBiomes();
        if (biomes instanceof PalettedContainer<Holder<Biome>> writableBiomes) {
            writableBiomes.getAndSet(x, y, z, biomeHolder);
        }
    }

    public Potion getPotionByTag(CompoundTag tag) {
        return PotionUtils.getPotion(tag);
    }

    public void tryToChopTree(Level level, BlockPos spawnPos, BlockPos breakingPos, BlockState blockState) {
        if (!blockState.is(BlockTags.LOGS)) return;
        TreeCutter.findTree(level, breakingPos, blockState)
                .destroyBlocks(level, null, (pPos, pStack) -> {
                    Vec3 dropPos = VecHelper.getCenterOf(pPos);
                    ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, pStack);
                    level.addFreshEntity(entity);
                });
    }

    public MobEffectInstance calculateEffectForQuality(ItemStack itemStack, int quality) {
        if (itemStack.getItem() instanceof DrinkBlockItemAccessor drinkBlockItem) {
            return drinkBlockItem.calculateEffect(quality);
        }
        return null;
    }

    public boolean launchTendonsAt(LivingEntity playerIn, Entity closestValid) {
        Level worldIn = playerIn.level();
        TidalTentacleCapability.ITentacleCapability tentacleCapability = ModCapabilities.getCapability(playerIn, ModCapabilities.TENTACLE_CAPABILITY);
        if (tentacleCapability != null && TidalTentacleUtil.canLaunchTentacles(worldIn, playerIn)) {
            TidalTentacleUtil.retractFarTentacles(worldIn, playerIn);
            if (!worldIn.isClientSide && closestValid != null) {
                Tidal_Tentacle_Entity segment = (Tidal_Tentacle_Entity) ((EntityType) ModEntities.TIDAL_TENTACLE.get()).create(worldIn);
                segment.copyPosition(playerIn);
                worldIn.addFreshEntity(segment);
                segment.setCreatorEntityUUID(playerIn.getUUID());
                segment.setFromEntityID(playerIn.getId());
                segment.setToEntityID(closestValid.getId());
                segment.copyPosition(playerIn);
                segment.setProgress(0.0F);
                TidalTentacleUtil.setLastTentacle(playerIn, segment);
                return true;
            }
        }
        return false;
    }

    public void spawnDamageNumber(ServerPlayer player, LivingEntity target, DamageSource source, float amount, CritRecord critRecord) {
        NetworkHandler.CHANNEL.sendToClientPlayer(player, new ClientBoundDamageNumberMessage(target, amount, source, critRecord));
    }

    public Entity getEntityById(Level level, int entityId) {
        return level.getEntity(entityId);
    }

    public Entity getEntityByUUID(ServerLevel level, UUID uuid){
        return level.getEntities().get(uuid);
    }
}