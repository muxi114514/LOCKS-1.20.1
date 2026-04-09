package com.max.lock.common.capability;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import com.max.lock.common.util.Lockable;




public interface ILockableStorage {
    
    Int2ObjectMap<Lockable> get();

    
    void add(Lockable lkb);

    
    void remove(int id);

    
    net.minecraft.nbt.ListTag serializeNBT();

    
    void deserializeNBT(net.minecraft.nbt.ListTag nbt);
}
