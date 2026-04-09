package com.max.lock.common.capability;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import com.max.lock.common.util.Lockable;

/**
 * Chunk 级锁存储接口 — 每个区块存储其包含的 Lockable 集合
 */
public interface ILockableStorage {
    /** 获取该区块存储的所有 Lockable */
    Int2ObjectMap<Lockable> get();

    /** 添加 Lockable 到此区块 */
    void add(Lockable lkb);

    /** 从此区块移除指定 ID 的 Lockable */
    void remove(int id);

    /** 序列化为 NBT */
    net.minecraft.nbt.ListTag serializeNBT();

    /** 从 NBT 反序列化 */
    void deserializeNBT(net.minecraft.nbt.ListTag nbt);
}
