package com.max.lock.client.gui.sprite.action;

import com.max.lock.client.gui.sprite.Sprite;

public class WaitAction<S extends Sprite> extends TimedAction<S> {
    public static <Z extends Sprite> WaitAction<Z> ticks(int ticks) {
        return (WaitAction<Z>) new WaitAction<Z>().time(ticks);
    }
}
