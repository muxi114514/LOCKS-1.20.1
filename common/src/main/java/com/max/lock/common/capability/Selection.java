package com.max.lock.common.capability;

import net.minecraft.core.BlockPos;




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
