package com.max.lock.common.init;

import com.max.lock.common.Lock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;




public final class LockItemTags {
    private LockItemTags() {
    }

    public static final TagKey<Item> KEYS = bind("keys"),
            LOCKS = bind("locks"),
            LOCK_PICKS = bind("lock_picks");

    public static TagKey<Item> bind(String name) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(Lock.MOD_ID, name));
    }
}
