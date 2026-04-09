package com.max.lock.common.capability;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

/**
 * 跨平台 Capability 访问桥接
 * Forge 端通过 Capability API 实现，Fabric 端通过 Attachment API 实现
 */
public final class LockCapabilityAccess {
    private LockCapabilityAccess() {
    }

    /** 获取 World 级锁管理器 */
    @ExpectPlatform
    @Nullable
    public static ILockableHandler getHandler(Level world) {
        throw new AssertionError("未实现平台方法");
    }

    /** 获取 Chunk 级锁存储 */
    @ExpectPlatform
    @Nullable
    public static ILockableStorage getStorage(LevelChunk chunk) {
        throw new AssertionError("未实现平台方法");
    }

    /** 获取玩家选区数据 */
    @ExpectPlatform
    @Nullable
    public static ISelection getSelection(Player player) {
        throw new AssertionError("未实现平台方法");
    }
}
