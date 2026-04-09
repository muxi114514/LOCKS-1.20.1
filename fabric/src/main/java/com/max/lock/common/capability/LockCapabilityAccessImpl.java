package com.max.lock.common.capability;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;






public class LockCapabilityAccessImpl {

    private static final Map<Level, ILockableHandler> WORLD_HANDLERS = new WeakHashMap<>();
    private static final Map<LevelChunk, ILockableStorage> CHUNK_STORAGES = new WeakHashMap<>();
    private static final Map<Player, ISelection> PLAYER_SELECTIONS = new WeakHashMap<>();



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

    
    public static void removeHandler(Level world) {
        WORLD_HANDLERS.remove(world);
    }

    
    public static void removeStorage(LevelChunk chunk) {
        CHUNK_STORAGES.remove(chunk);
    }
}
