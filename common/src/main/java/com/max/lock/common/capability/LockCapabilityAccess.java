package com.max.lock.common.capability;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;





public final class LockCapabilityAccess {
    private LockCapabilityAccess() {
    }

    
    @ExpectPlatform
    @Nullable
    public static ILockableHandler getHandler(Level world) {
        throw new AssertionError("未实现平台方法");
    }

    
    @ExpectPlatform
    @Nullable
    public static ILockableStorage getStorage(LevelChunk chunk) {
        throw new AssertionError("未实现平台方法");
    }

    
    @ExpectPlatform
    @Nullable
    public static ISelection getSelection(Player player) {
        throw new AssertionError("未实现平台方法");
    }
}
