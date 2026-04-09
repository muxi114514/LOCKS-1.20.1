package com.max.lock.common.capability;

import net.minecraft.core.BlockPos;

/**
 * 玩家选区接口 — freeLock 模式下存储第一个选择的方块位置
 */
public interface ISelection {
    BlockPos get();

    void set(BlockPos pos);
}
