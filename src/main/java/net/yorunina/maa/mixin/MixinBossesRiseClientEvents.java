package net.yorunina.maa.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.unusual.block_factorys_bosses.event.ClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientEvents.class)
public abstract class MixinBossesRiseClientEvents {
    @Inject(method = "renderRollGUI", at = @At("HEAD"), cancellable = true, remap = false)
    private static void maa$noRollHud(Minecraft minecraft, GuiGraphics guiGraphics, CallbackInfo ci) {
        ci.cancel();
    }
}