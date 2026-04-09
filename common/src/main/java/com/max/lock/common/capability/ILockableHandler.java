package com.max.lock.common.capability;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import com.max.lock.common.util.Lockable;
import net.minecraft.core.BlockPos;




public interface ILockableHandler {
    
    int nextId();

    
    Int2ObjectMap<Lockable> getLoaded();

    
    Int2ObjectMap<Lockable> getInChunk(BlockPos pos);

    
    boolean add(Lockable lkb);

    
    boolean remove(int id);

    
    int getCurrentId();

    
    void setCurrentId(int id);
}
