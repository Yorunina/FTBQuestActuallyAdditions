package net.yorunina.maa.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yorunina.maa.compat.kubejs.MAAEvents;
import net.yorunina.maa.compat.kubejs.events.FishingRetrieveEventJS;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = FishingHook.class)
public abstract class MixinFishingHook extends Entity {
    private MixinFishingHook(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Unique
    public boolean waiting = false;

    @Inject(method = "catchingFish", at = @At(value = "HEAD"), cancellable = true)
    private void catchingFishMixin(BlockPos p_37146_, CallbackInfo ci) {
        if (waiting) ci.cancel();
    }

    @Inject(method = "retrieve", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z"), cancellable = true)
    public void retrieveMixin(ItemStack p_37157_, CallbackInfoReturnable<Integer> cir, @Local List<ItemStack> items) {
        FishingHook hook = (FishingHook) (Object) this;
        ServerPlayer player = (ServerPlayer) hook.getPlayerOwner();
        if (player == null) return;

        if (MAAEvents.FISHING_RETRIEVE.post(new FishingRetrieveEventJS(player, hook, items)).arch().isFalse()) {
            cir.setReturnValue(0);
        }
    }
}