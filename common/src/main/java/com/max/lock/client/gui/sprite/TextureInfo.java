package com.max.lock.client.gui.sprite;

import com.max.lock.client.util.LocksClientUtil;
import com.mojang.blaze3d.vertex.PoseStack;

// Texture region descriptor with draw method
public class TextureInfo {
    public int startX, startY, width, height, canvasWidth, canvasHeight;

    public TextureInfo(int startX, int startY, int width, int height, int canvasWidth, int canvasHeight) {
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
    }

    public void draw(PoseStack mtx, float x, float y, float alpha) {
        LocksClientUtil.texture(mtx, x, y, this.startX, this.startY, this.width, this.height, this.canvasWidth,
                this.canvasHeight, alpha);
    }
}
