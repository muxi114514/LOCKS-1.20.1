package com.max.lock.common.network;

import com.max.lock.common.Lock;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * 网络通信注册中心
 * 使用 Architectury NetworkManager 实现跨平台网络通信
 */
public final class LockNetwork {
    // 频道 ID
    public static final ResourceLocation ADD_LOCKABLE = new ResourceLocation(Lock.MOD_ID, "add_lockable");
    public static final ResourceLocation ADD_LOCKABLE_CHUNK = new ResourceLocation(Lock.MOD_ID, "add_lockable_chunk");
    public static final ResourceLocation REMOVE_LOCKABLE = new ResourceLocation(Lock.MOD_ID, "remove_lockable");
    public static final ResourceLocation UPDATE_LOCKABLE = new ResourceLocation(Lock.MOD_ID, "update_lockable");
    public static final ResourceLocation TRY_PIN = new ResourceLocation(Lock.MOD_ID, "try_pin");
    public static final ResourceLocation TRY_PIN_RESULT = new ResourceLocation(Lock.MOD_ID, "try_pin_result");

    private LockNetwork() {
    }

    public static void register() {
        // 服务器→客户端
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, ADD_LOCKABLE, AddLockablePacket::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, ADD_LOCKABLE_CHUNK, AddLockableToChunkPacket::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, REMOVE_LOCKABLE, RemoveLockablePacket::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, UPDATE_LOCKABLE, UpdateLockablePacket::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, TRY_PIN_RESULT, TryPinResultPacket::handle);
        // 客户端→服务器
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, TRY_PIN, TryPinPacket::handle);
    }

    /** 向指定玩家发送 S2C 包 */
    public static void sendToPlayer(ServerPlayer player, ResourceLocation id, FriendlyByteBuf buf) {
        NetworkManager.sendToPlayer(player, id, buf);
    }

    /** 向服务器发送 C2S 包 */
    public static void sendToServer(ResourceLocation id, FriendlyByteBuf buf) {
        NetworkManager.sendToServer(id, buf);
    }

    /** 创建空 buf */
    public static FriendlyByteBuf createBuf() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }
}
