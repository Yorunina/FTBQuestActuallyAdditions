package net.yorunina.maa.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.unusual.block_factorys_bosses.entity.boss.yeti.YetiEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 修复 Bosses'Rise (block_factorys_bosses 2.1.2) 雪人 BOSS 在服务端主线程上
 * 因 getBlockState() 触发"同步加载区块"而导致的服务器看门狗超时 (ServerHangWatchdog)。
 *
 * todo 版本更新时记得检查是否还有这个问题，没问题就删了
 */
@Mixin(YetiEntity.class)
public abstract class MixinYetiEntity {

    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    )
    private BlockState maa$yetigetBlockStateSafe(Level level, BlockPos pos, Operation<BlockState> original) {
        // 目标区块未加载时, 不要用 getBlockState() 去强制同步加载区块,
        // 否则会在服务端主线程卡住, 导致 tick 超时。
        if (pos == null || !level.hasChunkAt(pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return original.call(level, pos);
    }
}