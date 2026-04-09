package com.max.lock.common.network;

import com.max.lock.common.capability.ILockableHandler;
import com.max.lock.common.capability.LockCapabilityAccess;
import com.max.lock.common.util.Lockable;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;




public class UpdateLockablePacket {
    private UpdateLockablePacket() {
    }

    public static void send(ServerPlayer player, Lockable lkb) {
        FriendlyByteBuf buf = LockNetwork.createBuf();
        buf.writeInt(lkb.id);
        buf.writeBoolean(lkb.lock.isLocked());
        LockNetwork.sendToPlayer(player, LockNetwork.UPDATE_LOCKABLE, buf);
    }

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        int id = buf.readInt();
        boolean locked = buf.readBoolean();
        ctx.queue(() -> {
            ILockableHandler handler = LockCapabilityAccess.getHandler(Minecraft.getInstance().level);
            if (handler == null)
                return;
            Lockable lkb = handler.getLoaded().get(id);
            if (lkb != null)
                lkb.lock.setLocked(locked);
        });
    }
}
