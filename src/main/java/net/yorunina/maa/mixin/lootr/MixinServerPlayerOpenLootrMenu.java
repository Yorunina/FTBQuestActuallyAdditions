package net.yorunina.maa.mixin.lootr;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.network.NetworkHooks;
import net.yorunina.maa.compat.lootr.MAALootrInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayerOpenLootrMenu {
    @Inject(
            method = "openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void maa$openLootrMenu(MenuProvider provider, CallbackInfoReturnable<OptionalInt> cir) {
        if (!(provider instanceof MAALootrInventory inventory) || !inventory.maa$shouldUseCustomMenu()) {
            return;
        }
        ServerPlayer player = (ServerPlayer) (Object) this;
        int rows = inventory.maa$prepareOpen(player);
        NetworkHooks.openScreen(player, provider, buf -> buf.writeVarInt(rows));
        cir.setReturnValue(player.containerMenu == player.inventoryMenu
                ? OptionalInt.empty()
                : OptionalInt.of(player.containerMenu.containerId));
    }
}