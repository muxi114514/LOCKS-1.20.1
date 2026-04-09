package com.max.lock.common.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * S2C：服务端返回撬锁结果
 */
public class TryPinResultPacket {
    private TryPinResultPacket() {
    }

    public static void send(ServerPlayer player, boolean correct, int resetCount) {
        FriendlyByteBuf buf = LockNetwork.createBuf();
        buf.writeBoolean(correct);
        buf.writeVarInt(resetCount);
        LockNetwork.sendToPlayer(player, LockNetwork.TRY_PIN_RESULT, buf);
    }

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        boolean correct = buf.readBoolean();
        int resetCount = buf.readVarInt();
        ctx.queue(() -> {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player == null)
                return;
            net.minecraft.world.inventory.AbstractContainerMenu container = mc.player.containerMenu;
            if (container instanceof com.max.lock.common.menu.LockPickingMenu menu)
                menu.handlePin(correct, resetCount);
            if (mc.screen instanceof com.max.lock.client.gui.LockPickingScreen screen)
                screen.handlePin(correct, resetCount);
        });
    }
}
