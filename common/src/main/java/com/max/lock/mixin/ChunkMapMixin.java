package com.max.lock.mixin;

import com.max.lock.common.capability.ILockableStorage;
import com.max.lock.common.capability.LockCapabilityAccess;
import com.max.lock.common.network.AddLockableToChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;





@Mixin(ChunkMap.class)
public class ChunkMapMixin {
    @Inject(at = @At("TAIL"), method = "playerLoadedChunk")
    private void locks$playerLoadedChunk(ServerPlayer player,
            MutableObject<ClientboundLevelChunkWithLightPacket> packets,
            LevelChunk chunk, CallbackInfo ci) {
        ILockableStorage storage = LockCapabilityAccess.getStorage(chunk);
        if (storage == null)
            return;
        storage.get().values().forEach(lkb -> AddLockableToChunkPacket.send(player, lkb,
                chunk.getPos().x, chunk.getPos().z));
    }
}
