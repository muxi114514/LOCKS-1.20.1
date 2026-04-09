package com.max.lock.common.util;

import net.minecraft.server.level.WorldGenRegion;

/**
 * 世界生成阶段的上下文传递
 * 
 * Mixin 不允许非私有静态字段，因此将 ThreadLocal 放在独立工具类中。
 * 由 WorldGenRegionMixin 设置，由 LootTableContainerMixin 读取。
 */
public final class WorldGenContext {
    private WorldGenContext() {
    }

    /**
     * 当前线程正在处理的 WorldGenRegion
     * 在 WorldGenRegion.setBlock 时设置，供 tryLoadLootTable 路径使用
     */
    public static final ThreadLocal<WorldGenRegion> CURRENT_REGION = new ThreadLocal<>();
}
