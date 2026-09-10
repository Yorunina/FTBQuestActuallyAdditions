package net.yorunina.maa.mixin.lootr;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.yorunina.maa.compat.lootr.MAALootrChestMenu;
import noobanidus.mods.lootr.block.entities.LootrChestBlockEntity;
import noobanidus.mods.lootr.data.SpecialChestInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "noobanidus.mods.lootr.block.entities.LootrChestBlockEntity$1", remap = false)
public abstract class MixinLootrChestOpenersCounter {
    @Shadow
    @Final
    private LootrChestBlockEntity this$0;

    @Inject(method = "m_142718_", at = @At("HEAD"), cancellable = true)
    private void maa$recognizeCustomMenu(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!(player.containerMenu instanceof MAALootrChestMenu menu)) {
            return;
        }
        Container container = menu.getContainer();
        if (container instanceof SpecialChestInventory inventory && this.this$0.getTileId().equals(inventory.getTileId())) {
            cir.setReturnValue(true);
        }
    }
}