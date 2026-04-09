package com.max.lock.common.capability;

import java.util.concurrent.atomic.AtomicInteger;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import com.max.lock.common.network.AddLockablePacket;
import com.max.lock.common.network.RemoveLockablePacket;
import com.max.lock.common.network.UpdateLockablePacket;
import com.max.lock.common.util.Lockable;
import com.max.lock.common.util.LockableListener;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * World 级锁管理器实现
 * 管理所有已加载的 Lockable，负责跨区块协调和客户端同步
 */
public class LockableHandler implements ILockableHandler, LockableListener {
    private final Level world;
    private final AtomicInteger nextId = new AtomicInteger();
    private final Int2ObjectMap<Lockable> loaded = new Int2ObjectLinkedOpenHashMap<>();

    public LockableHandler(Level world) {
        this.world = world;
    }

    @Override
    public int nextId() {
        return this.nextId.getAndIncrement();
    }

    @Override
    public Int2ObjectMap<Lockable> getLoaded() {
        return this.loaded;
    }

    @Override
    public Int2ObjectMap<Lockable> getInChunk(BlockPos pos) {
        LevelChunk chunk = this.world.getChunkAt(pos);
        ILockableStorage storage = LockCapabilityAccess.getStorage(chunk);
        return storage != null ? storage.get() : new Int2ObjectLinkedOpenHashMap<>();
    }

    @Override
    public boolean add(Lockable lkb) {
        if (this.loaded.containsKey(lkb.id))
            return false;
        this.loaded.put(lkb.id, lkb);
        lkb.addListener(this);
        // 将 Lockable 添加到所有覆盖的区块
        lkb.bb.getContainedChunks((cx, cz) -> {
            LevelChunk chunk = this.world.getChunk(cx, cz);
            ILockableStorage storage = LockCapabilityAccess.getStorage(chunk);
            if (storage != null)
                storage.add(lkb);
            return false;
        });
        // 服务端：同步到客户端
        if (!this.world.isClientSide && this.world instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.players())
                AddLockablePacket.send(player, lkb);
        }
        // 客户端：播放晃动动画
        if (this.world.isClientSide)
            lkb.swing(10);
        return true;
    }

    @Override
    public boolean remove(int id) {
        Lockable lkb = this.loaded.remove(id);
        if (lkb == null)
            return false;
        lkb.removeListener(this);
        // 从所有覆盖的区块移除
        lkb.bb.getContainedChunks((cx, cz) -> {
            LevelChunk chunk = this.world.getChunk(cx, cz);
            ILockableStorage storage = LockCapabilityAccess.getStorage(chunk);
            if (storage != null)
                storage.remove(id);
            return false;
        });
        // 服务端：同步到客户端
        if (!this.world.isClientSide && this.world instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.players())
                RemoveLockablePacket.send(player, id);
        }
        return true;
    }

    @Override
    public int getCurrentId() {
        return this.nextId.get();
    }

    @Override
    public void setCurrentId(int id) {
        this.nextId.set(id);
    }

    @Override
    public void onChanged(Object source) {
        if (!(source instanceof Lockable lkb))
            return;
        // 标记区块需要保存
        lkb.bb.getContainedChunks((cx, cz) -> {
            LevelChunk chunk = this.world.getChunk(cx, cz);
            chunk.setUnsaved(true);
            return false;
        });
        // 服务端：同步锁状态更新到客户端
        if (!this.world.isClientSide && this.world instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.players())
                UpdateLockablePacket.send(player, lkb);
        }
    }
}
