package com.max.lock.client.gui.sprite.action;

import com.max.lock.client.gui.sprite.Sprite;

public abstract class TimedAction<S extends Sprite> extends SingleCallbackAction<S> {
    public int ticks;

    public TimedAction<S> time(int ticks) {
        this.ticks = ticks;
        return this;
    }

    @Override
    public boolean isFinished(S sprite) {
        return this.ticks == 0;
    }

    @Override
    public void update(S sprite) {
        --this.ticks;
    }
}
