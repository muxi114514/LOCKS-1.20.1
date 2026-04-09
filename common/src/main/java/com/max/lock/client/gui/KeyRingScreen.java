package com.max.lock.client.gui;

import com.max.lock.common.menu.KeyRingMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

// Key ring inventory screen
public class KeyRingScreen extends AbstractContainerScreen<KeyRingMenu> {
    public static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    public KeyRingScreen(KeyRingMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 114 + menu.rows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        super.render(gfx, mouseX, mouseY, partialTick);
        this.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int rows = this.getMenu().rows;
        RenderSystem.setShaderTexture(0, TEXTURE);
        int cornerX = (this.width - this.imageWidth) / 2;
        int cornerY = (this.height - this.imageHeight) / 2;
        gfx.blit(TEXTURE, cornerX, cornerY, 0, 0, this.imageWidth, rows * 18 + 17);
        gfx.blit(TEXTURE, cornerX, cornerY + rows * 18 + 17, 0, 126, this.imageWidth, 96);
    }
}
