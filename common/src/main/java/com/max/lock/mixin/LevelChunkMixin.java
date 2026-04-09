package com.max.lock.mixin;

import com.max.lock.common.capability.ILockableHandler;
import com.max.lock.common.capability.ILockableStorage;
import com.max.lock.common.capability.LockCapabilityAccess;
import com.max.lock.common.capability.LockableHandler;
import com.max.lock.common.network.AddLockableToChunkPacket;
import com.max.lock.common.util.ILockableProvider;
import com.max.lock.common.util.Lockable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;




@Mixin(LevelChunk.class)
public class LevelChunkMixin {
    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ProtoChunk;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;)V")
    private void locks$initFromProto(ServerLevel world, ProtoChunk proto,
            LevelChunk.PostLoadProcessor postLoad, CallbackInfo ci) {
        if (!(proto instanceof ILockableProvider provider))
            return;
        LevelChunk chunk = (LevelChunk) (Object) this;
        ILockableStorage storage = LockCapabilityAccess.getStorage(chunk);
        ILockableHandler handler = LockCapabilityAccess.getHandler(world);
        if (storage == null || handler == null)
            return;
        for (Lockable lkb : provider.getLockables()) {
            storage.add(lkb);
            handler.getLoaded().put(lkb.id, lkb);

            if (handler instanceof LockableHandler lh)
                lkb.addListener(lh);

            for (ServerPlayer player : world.players())
                AddLockableToChunkPacket.send(player, lkb,
                        chunk.getPos().x, chunk.getPos().z);
        }
    }
}
