package com.max.lock.client.gui.sprite.action;

import java.util.function.BiConsumer;
import com.max.lock.client.gui.sprite.Sprite;

public abstract class SingleCallbackAction<S extends Sprite> implements IAction<S> {
    protected BiConsumer<IAction<S>, S> callback;

    @Override
    public IAction<S> then(BiConsumer<IAction<S>, S> cb) {
        this.callback = cb;
        return this;
    }

    @Override
    public void finish(S sprite) {
        if (this.callback != null)
            this.callback.accept(this, sprite);
    }
}
