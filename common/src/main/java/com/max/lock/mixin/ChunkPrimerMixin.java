package com.max.lock.mixin;

import java.util.ArrayList;
import java.util.List;

import com.max.lock.common.util.ILockableProvider;
import com.max.lock.common.util.Lockable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.level.chunk.ProtoChunk;




@Mixin(ProtoChunk.class)
public class ChunkPrimerMixin implements ILockableProvider {
    @Unique
    private final List<Lockable> locks$lockables = new ArrayList<>();

    @Override
    public List<Lockable> getLockables() {
        return this.locks$lockables;
    }
}
