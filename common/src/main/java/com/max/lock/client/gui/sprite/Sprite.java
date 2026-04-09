package com.max.lock.client.gui.sprite;

import java.util.ArrayDeque;
import java.util.Queue;

import com.max.lock.client.gui.sprite.action.IAction;
import com.max.lock.client.util.LocksClientUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

// Animated GUI sprite with action queue
public class Sprite {
    private final Queue<IAction> actions = new ArrayDeque<>(4);
    public TextureInfo tex;
    public float posX, posY, oldPosX, oldPosY, speedX, speedY;
    public float rot, oldRot, rotSpeed, originX, originY;
    public float alpha = 1f, oldAlpha = 1f;

    public Sprite(TextureInfo tex) {
        this.tex = tex;
    }

    public Sprite position(float posX, float posY) {
        this.posX = this.oldPosX = posX;
        this.posY = this.oldPosY = posY;
        return this;
    }

    public Sprite rotation(float rot, float originX, float originY) {
        this.rot = this.oldRot = rot;
        this.originX = originX;
        this.originY = originY;
        return this;
    }

    public Sprite alpha(float alpha) {
        this.alpha = this.oldAlpha = alpha;
        return this;
    }

    @SuppressWarnings("unchecked")
    public void execute(IAction... actions) {
        for (IAction action : actions)
            this.actions.offer(action);
    }

    public boolean isExecuting() {
        return !this.actions.isEmpty();
    }

    public void draw(PoseStack mtx, float partialTick) {
        if (this.alpha <= 0f)
            return;
        mtx.pushPose();
        mtx.translate(this.originX, this.originY, 0f);
        mtx.mulPose(Axis.ZP.rotationDegrees(LocksClientUtil.lerp(this.oldRot, this.rot, partialTick)));
        mtx.translate(-this.originX, -this.originY, 0f);
        this.tex.draw(mtx,
                LocksClientUtil.lerp(this.oldPosX, this.posX, partialTick),
                LocksClientUtil.lerp(this.oldPosY, this.posY, partialTick),
                LocksClientUtil.lerp(this.oldAlpha, this.alpha, partialTick));
        mtx.popPose();
    }

    @SuppressWarnings("unchecked")
    public void update() {
        this.oldPosX = this.posX;
        this.oldPosY = this.posY;
        this.oldRot = this.rot;
        this.oldAlpha = this.alpha;
        this.posX += this.speedX;
        this.posY += this.speedY;
        this.rot += this.rotSpeed;
        IAction action = this.actions.peek();
        if (action == null)
            return;
        if (action.isFinished(this))
            this.actions.poll().finish(this);
        else
            action.update(this);
    }
}
