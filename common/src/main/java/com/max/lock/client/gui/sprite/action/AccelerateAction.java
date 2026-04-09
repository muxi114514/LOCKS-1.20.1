package com.max.lock.client.gui.sprite.action;

import com.max.lock.client.gui.sprite.Sprite;

public class AccelerateAction<S extends Sprite> extends MoveAction<S> {
    public float accelX, accelY;

    public AccelerateAction(float speedX, float speedY, float accelX, float accelY) {
        super(speedX, speedY);
        this.accelX = accelX;
        this.accelY = accelY;
    }

    public static <Z extends Sprite> AccelerateAction<Z> at(float speedX, float speedY, float accelX, float accelY) {
        return new AccelerateAction<>(speedX, speedY, accelX, accelY);
    }

    public static <Z extends Sprite> AccelerateAction<Z> to(float shiftX, float shiftY, int ticks, boolean accel) {
        float speedX = 2f * shiftX / ticks / (accel ? 3f : 1f);
        float speedY = 2f * shiftY / ticks / (accel ? 3f : 1f);
        float ax = speedX / ticks * (accel ? 1f : -1f);
        float ay = speedY / ticks * (accel ? 1f : -1f);
        return (AccelerateAction<Z>) at(speedX, speedY, ax, ay).time(ticks);
    }

    @Override
    public void update(S sprite) {
        super.update(sprite);
        sprite.posX += this.accelX * 0.5f;
        sprite.posY += this.accelY * 0.5f;
        this.speedX += this.accelX;
        this.speedY += this.accelY;
    }
}
