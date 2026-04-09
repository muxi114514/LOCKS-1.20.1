package com.max.lock.client.init;

import com.max.lock.common.Lock;
import com.max.lock.common.init.LockItems;
import com.max.lock.common.item.LockItem;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;





public final class LockItemModelsProperties {
    private LockItemModelsProperties() {
    }

    public static void register() {
        ResourceLocation openId = new ResourceLocation(Lock.MOD_ID, "open");
        ClampedItemPropertyFunction openGetter = (stack, world, entity, seed) -> LockItem.isOpen(stack) ? 1f : 0f;
        ItemProperties.register(LockItems.WOOD_LOCK.get(), openId, openGetter);
        ItemProperties.register(LockItems.IRON_LOCK.get(), openId, openGetter);
        ItemProperties.register(LockItems.STEEL_LOCK.get(), openId, openGetter);
        ItemProperties.register(LockItems.GOLD_LOCK.get(), openId, openGetter);
        ItemProperties.register(LockItems.DIAMOND_LOCK.get(), openId, openGetter);
    }
}
