package com.max.lock.client.gui.sprite.action;

import java.util.function.BiConsumer;
import com.max.lock.client.gui.sprite.Sprite;

public interface IAction<S extends Sprite> {
    boolean isFinished(S sprite);

    void update(S sprite);

    void finish(S sprite);

    IAction<S> then(BiConsumer<IAction<S>, S> cb);
}
