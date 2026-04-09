package com.max.lock.common.capability;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import com.max.lock.common.util.Lockable;
import net.minecraft.core.BlockPos;

/**
 * World 级锁管理器接口 — 管理所有已加载的 Lockable 实例
 */
public interface ILockableHandler {
    /** 生成下一个唯一锁 ID */
    int nextId();

    /** 获取所有已加载的 Lockable */
    Int2ObjectMap<Lockable> getLoaded();

    /** 获取指定位置所在区块的所有 Lockable */
    Int2ObjectMap<Lockable> getInChunk(BlockPos pos);

    /** 添加 Lockable 到所有覆盖的区块 */
    boolean add(Lockable lkb);

    /** 从所有区块移除指定 ID 的 Lockable */
    boolean remove(int id);

    /** 获取当前 ID 计数器值 */
    int getCurrentId();

    /** 设置 ID 计数器值（用于反序列化） */
    void setCurrentId(int id);
}
