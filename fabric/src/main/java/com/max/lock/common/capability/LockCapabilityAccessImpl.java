package com.max.lock.common.capability;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric 平台 @ExpectPlatform 实现
 * 使用 WeakHashMap 手动管理附件数据
 * WeakHashMap 确保当 World/Chunk/Player 被 GC 回收时附件数据自动清理
 */
public class LockCapabilityAccessImpl {
    // 使用 WeakHashMap 避免内存泄漏
    private static final Map<Level, ILockableHandler> WORLD_HANDLERS = new WeakHashMap<>();
    private static final Map<LevelChunk, ILockableStorage> CHUNK_STORAGES = new WeakHashMap<>();
    private static final Map<Player, ISelection> PLAYER_SELECTIONS = new WeakHashMap<>();

    // ===== @ExpectPlatform 实现 =====

    @Nullable
    public static ILockableHandler getHandler(Level world) {
        return WORLD_HANDLERS.computeIfAbsent(world, LockableHandler::new);
    }

    @Nullable
    public static ILockableStorage getStorage(LevelChunk chunk) {
        return CHUNK_STORAGES.computeIfAbsent(chunk, LockableStorage::new);
    }

    @Nullable
    public static ISelection getSelection(Player player) {
        return PLAYER_SELECTIONS.computeIfAbsent(player, p -> new Selection());
    }

    /** 清理指定 World 的数据 */
    public static void removeHandler(Level world) {
        WORLD_HANDLERS.remove(world);
    }

    /** 清理指定 Chunk 的数据 */
    public static void removeStorage(LevelChunk chunk) {
        CHUNK_STORAGES.remove(chunk);
    }
}
