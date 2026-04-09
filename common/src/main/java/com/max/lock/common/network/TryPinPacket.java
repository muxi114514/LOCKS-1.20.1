package com.max.lock.common.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * C2S：客户端发送撬锁尝试（选择了哪个 pin）
 */
public class TryPinPacket {
    private TryPinPacket() {
    }

    /** 客户端发送 */
    public static void send(byte pin) {
        FriendlyByteBuf buf = LockNetwork.createBuf();
        buf.writeByte(pin);
        LockNetwork.sendToServer(LockNetwork.TRY_PIN, buf);
    }

    /** 服务端接收处理 */
    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        byte pin = buf.readByte();
        ctx.queue(() -> {
            net.minecraft.world.inventory.AbstractContainerMenu container = ctx.getPlayer().containerMenu;
            if (container instanceof com.max.lock.common.menu.LockPickingMenu menu)
                menu.tryPin(pin);
        });
    }
}
