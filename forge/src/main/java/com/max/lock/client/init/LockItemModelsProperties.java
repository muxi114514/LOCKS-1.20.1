package com.max.lock.client.init;

import com.max.lock.common.Lock;
import com.max.lock.common.init.LockItems;
import com.max.lock.common.item.LockItem;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

/**
 * 物品模型属性注册（Forge 客户端）
 * 为锁物品注册 "open" 属性，根据 NBT 切换开/关锁模型
 */
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
