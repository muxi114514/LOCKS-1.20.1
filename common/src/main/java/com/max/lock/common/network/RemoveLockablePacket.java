package com.max.lock.common.network;

import com.max.lock.common.capability.ILockableHandler;
import com.max.lock.common.capability.LockCapabilityAccess;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * S2C：通知客户端移除 Lockable
 */
public class RemoveLockablePacket {
    private RemoveLockablePacket() {
    }

    public static void send(ServerPlayer player, int id) {
        FriendlyByteBuf buf = LockNetwork.createBuf();
        buf.writeInt(id);
        LockNetwork.sendToPlayer(player, LockNetwork.REMOVE_LOCKABLE, buf);
    }

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        int id = buf.readInt();
        ctx.queue(() -> {
            ILockableHandler handler = LockCapabilityAccess.getHandler(Minecraft.getInstance().level);
            if (handler != null)
                handler.remove(id);
        });
    }
}
