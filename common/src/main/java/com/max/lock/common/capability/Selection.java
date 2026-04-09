package com.max.lock.common.capability;

import net.minecraft.core.BlockPos;

/**
 * ISelection 实现类 — 存储 freeLock 模式的第一个选择位置
 */
public class Selection implements ISelection {
    private BlockPos pos;

    @Override
    public BlockPos get() {
        return this.pos;
    }

    @Override
    public void set(BlockPos pos) {
        this.pos = pos;
    }
}
