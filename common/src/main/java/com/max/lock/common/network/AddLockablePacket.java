package com.max.lock.common.network;

import com.max.lock.common.capability.ILockableHandler;
import com.max.lock.common.capability.LockCapabilityAccess;
import com.max.lock.common.util.Lockable;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;




public class AddLockablePacket {
    private AddLockablePacket() {
    }

    
    public static void send(ServerPlayer player, Lockable lkb) {
        FriendlyByteBuf buf = LockNetwork.createBuf();
        Lockable.toBuf(buf, lkb);
        LockNetwork.sendToPlayer(player, LockNetwork.ADD_LOCKABLE, buf);
    }

    
    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        Lockable lkb = Lockable.fromBuf(buf);
        ctx.queue(() -> {
            ILockableHandler handler = LockCapabilityAccess.getHandler(Minecraft.getInstance().level);
            if (handler != null)
                handler.add(lkb);
        });
    }
}
