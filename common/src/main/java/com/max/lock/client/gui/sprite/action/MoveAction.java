package com.max.lock.client.gui.sprite.action;

import com.max.lock.client.gui.sprite.Sprite;

public class MoveAction<S extends Sprite> extends TimedAction<S> {
    public float speedX, speedY;

    public MoveAction(float speedX, float speedY) {
        this.speedX = speedX;
        this.speedY = speedY;
    }

    public static <Z extends Sprite> MoveAction<Z> at(float speedX, float speedY) {
        return new MoveAction<>(speedX, speedY);
    }

    public static <Z extends Sprite> MoveAction<Z> to(float shiftX, float shiftY, int ticks) {
        return (MoveAction<Z>) at(shiftX / ticks, shiftY / ticks).time(ticks);
    }

    public static <Z extends Sprite> MoveAction<Z> to(Sprite sprite, float posX, float posY, int ticks) {
        return to(posX - sprite.posX, posY - sprite.posY, ticks);
    }

    @Override
    public void update(S sprite) {
        super.update(sprite);
        sprite.posX += this.speedX;
        sprite.posY += this.speedY;
    }
}
