package com.max.lock.client.gui.sprite;

import com.max.lock.client.util.LocksClientUtil;
import com.mojang.blaze3d.vertex.PoseStack;

// Spring sprite - dynamically switches texture based on target position
public class SpringSprite extends Sprite {
    public final TextureInfo[] texs;
    public Sprite target;

    public SpringSprite(TextureInfo[] texs, Sprite target) {
        super(texs[0]);
        this.texs = texs;
        this.target = target;
    }

    @Override
    public void draw(PoseStack mtx, float partialTick) {
        for (TextureInfo tex : this.texs)
            if (LocksClientUtil.lerp(this.target.oldPosY, this.target.posY, partialTick) < this.posY + tex.height)
                this.tex = tex;
        super.draw(mtx, partialTick);
    }
}
