package com.max.lock.common.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * 轻量级锁信息传输对象，用于 NBT 持久化时不需完整 Lockable 功能的场景
 */
public class LockableInfo {
    public final Cuboid6i bb;
    public final LockData lock;
    public final Transform tr;
    public final ItemStack stack;
    public final int id;

    public LockableInfo(Cuboid6i bb, LockData lock, Transform tr, ItemStack stack, int id) {
        this.bb = bb;
        this.lock = lock;
        this.tr = tr;
        this.stack = stack;
        this.id = id;
    }

    public static LockableInfo fromNbt(CompoundTag nbt) {
        return new LockableInfo(
                Cuboid6i.fromNbt(nbt.getCompound(Lockable.KEY_BB)),
                LockData.fromNbt(nbt.getCompound(Lockable.KEY_LOCK)),
                Transform.values()[(int) nbt.getByte(Lockable.KEY_TRANSFORM)],
                ItemStack.of(nbt.getCompound(Lockable.KEY_STACK)),
                nbt.getInt(Lockable.KEY_ID));
    }

    public static CompoundTag toNbt(LockableInfo lkb) {
        CompoundTag nbt = new CompoundTag();
        nbt.put(Lockable.KEY_BB, Cuboid6i.toNbt(lkb.bb));
        nbt.put(Lockable.KEY_LOCK, LockData.toNbt(lkb.lock));
        nbt.putByte(Lockable.KEY_TRANSFORM, (byte) lkb.tr.ordinal());
        CompoundTag stackTag = new CompoundTag();
        lkb.stack.save(stackTag);
        nbt.put(Lockable.KEY_STACK, stackTag);
        nbt.putInt(Lockable.KEY_ID, lkb.id);
        return nbt;
    }
}
