package com.max.lock.common.capability;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import com.max.lock.common.util.Lockable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.chunk.LevelChunk;





public class LockableStorage implements ILockableStorage {
    private final LevelChunk chunk;
    private final Int2ObjectMap<Lockable> lockables = new Int2ObjectLinkedOpenHashMap<>();

    public LockableStorage(LevelChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public Int2ObjectMap<Lockable> get() {
        return this.lockables;
    }

    @Override
    public void add(Lockable lkb) {
        this.lockables.put(lkb.id, lkb);
        this.chunk.setUnsaved(true);
    }

    @Override
    public void remove(int id) {
        this.lockables.remove(id);
        this.chunk.setUnsaved(true);
    }

    @Override
    public ListTag serializeNBT() {
        ListTag list = new ListTag();
        for (Lockable lkb : this.lockables.values())
            list.add(Lockable.toNbt(lkb));
        return list;
    }

    @Override
    public void deserializeNBT(ListTag nbt) {

        ILockableHandler handler = LockCapabilityAccess.getHandler(this.chunk.getLevel());
        if (handler == null)
            return;
        Int2ObjectMap<Lockable> loaded = handler.getLoaded();
        for (int a = 0; a < nbt.size(); ++a) {
            CompoundTag nbt1 = nbt.getCompound(a);
            int id = Lockable.idFromNbt(nbt1);
            Lockable lkb = loaded.get(id);
            if (lkb == null) {

                lkb = Lockable.fromNbt(nbt1);
                loaded.put(lkb.id, lkb);
                if (handler instanceof LockableHandler lh) {
                    lkb.addListener(lh);
                }
            }
            this.lockables.put(lkb.id, lkb);
        }
    }
}
