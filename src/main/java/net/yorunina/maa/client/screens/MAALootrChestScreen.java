package net.yorunina.maa.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yorunina.maa.compat.lootr.MAALootrChestMenu;

public class MAALootrChestScreen extends AbstractContainerScreen<MAALootrChestMenu> {
    private static final ResourceLocation TEX = ResourceLocation.parse("textures/gui/container/generic_54.png");
    private static final int TOP_H = 17;
    private static final int ROW_H = 18;
    private static final int GAP_H = 14;
    private static final int GAP_TEX_Y = 125;
    private static final int PLAYER_INV_H = 83;
    private static final int PLAYER_INV_TEX_Y = 139;

    private final int rows;

    public MAALootrChestScreen(MAALootrChestMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.rows = menu.getRows();
        this.imageWidth = 176;
        this.imageHeight = this.rows * ROW_H + 114;
        this.titleLabelY = 6;
        this.inventoryLabelY = this.rows * ROW_H + 20;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEX);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        graphics.blit(TEX, x, y, 0, 0, this.imageWidth, TOP_H);
        for (int row = 0; row < this.rows; row++) {
            int texY = row == 0 ? 17 : (row == this.rows - 1 ? 107 : 35);
            graphics.blit(TEX, x, y + TOP_H + row * ROW_H, 0, texY, this.imageWidth, ROW_H);
        }
        int gapY = y + TOP_H + this.rows * ROW_H;
        graphics.blit(TEX, x, gapY, 0, GAP_TEX_Y, this.imageWidth, GAP_H);
        graphics.blit(TEX, x, gapY + GAP_H, 0, PLAYER_INV_TEX_Y, this.imageWidth, PLAYER_INV_H);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}