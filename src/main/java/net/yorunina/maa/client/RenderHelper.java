package net.yorunina.maa.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.yorunina.maa.client.vfx.PunchVFXManager;

public class RenderHelper {
    public static RenderHelper INSTANCE = new RenderHelper();

    public static void spawnShockwaveByStyle(int durationTicks, PunchVFXManager.ShockwaveStyle style) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        Player player = client.player;
        Vec3 eyePos = player.getEyePosition(0.0F);
        Vec3 lookVec = player.getViewVector(0.0F);
        Vec3 impactCenter = eyePos.add(lookVec.scale(style.forwardOffset));

        PunchVFXManager.addShockwave(
                durationTicks, style,
                impactCenter,
                player.getViewYRot(0.0F),
                player.getViewXRot(0.0F)
        );
    }

    public static void spawnShockwave(int durationTicks) {
        spawnShockwaveByStyle(durationTicks, PunchVFXManager.ShockwaveStyle.DEFAULT);
    }

    public static void spawnShockwaveAt(int durationTicks, PunchVFXManager.ShockwaveStyle style, Vec3 position, float yRot, float xRot) {
        PunchVFXManager.addShockwave(durationTicks, style, position, yRot, xRot);
    }

    public void renderEntity(GuiGraphics guiGraphics, int x, int y, double scale, double yaw, double pitch, LivingEntity livingEntity) {
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.mulPoseMatrix(guiGraphics.pose().last().pose());
        modelViewStack.translate(x, y, 50.0F);
        modelViewStack.scale((float) -scale, (float) scale, (float) scale);
        PoseStack mobPoseStack = new PoseStack();
        mobPoseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        mobPoseStack.mulPose(Axis.XN.rotationDegrees(((float) Math.atan((pitch / 40.0F))) * 20.0F));
        livingEntity.yo = (float) Math.atan(yaw / 40.0F) * 20.0F;
        float yRot = (float) Math.atan(yaw / 40.0F) * 40.0F;
        float xRot = -((float) Math.atan(pitch / 40.0F)) * 20.0F;
        livingEntity.setYRot(yRot);
        livingEntity.setYRot(yRot);
        livingEntity.setXRot(xRot);
        livingEntity.yHeadRot = yRot;
        livingEntity.yHeadRotO = yRot;
        mobPoseStack.translate(0.0F, livingEntity.getY(), 0.0F);
        RenderSystem.applyModelViewMatrix();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        entityRenderDispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> {
            entityRenderDispatcher.render(livingEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, mobPoseStack, bufferSource, 15728880);
        });
        bufferSource.endBatch();
        entityRenderDispatcher.setRenderShadow(true);
        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public boolean isKeyDown(int keyCode) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keyCode);
    }
}