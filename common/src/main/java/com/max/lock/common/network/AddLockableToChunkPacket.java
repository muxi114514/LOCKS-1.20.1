package com.max.lock.common.network;

import com.max.lock.common.capability.ILockableHandler;
import com.max.lock.common.capability.ILockableStorage;
import com.max.lock.common.capability.LockCapabilityAccess;
import com.max.lock.common.capability.LockableHandler;
import com.max.lock.common.util.Lockable;
import dev.architectury.networking.NetworkManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;




public class AddLockableToChunkPacket {
    private AddLockableToChunkPacket() {
    }

    
    public static void send(ServerPlayer player, Lockable lkb, int chunkX, int chunkZ) {
        FriendlyByteBuf buf = LockNetwork.createBuf();
        Lockable.toBuf(buf, lkb);
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        LockNetwork.sendToPlayer(player, LockNetwork.ADD_LOCKABLE_CHUNK, buf);
    }

    
    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        Lockable lkb = Lockable.fromBuf(buf);
        int x = buf.readInt();
        int z = buf.readInt();
        ctx.queue(() -> {
            Level world = Minecraft.getInstance().level;
            if (world == null)
                return;
            ILockableHandler handler = LockCapabilityAccess.getHandler(world);
            if (handler == null)
                return;
            ILockableStorage storage = LockCapabilityAccess.getStorage(world.getChunk(x, z));
            if (storage == null)
                return;

            Int2ObjectMap<Lockable> loaded = handler.getLoaded();
            Lockable existing = loaded.get(lkb.id);
            if (existing == null) {
                existing = lkb;
                if (handler instanceof LockableHandler lh) {
                    lkb.addListener(lh);
                }
                loaded.put(lkb.id, lkb);
            }
            storage.add(existing);
        });
    }
}
