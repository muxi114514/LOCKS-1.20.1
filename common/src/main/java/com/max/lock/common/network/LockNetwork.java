package com.max.lock.common.network;

import com.max.lock.common.Lock;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;





public final class LockNetwork {

    public static final ResourceLocation ADD_LOCKABLE = new ResourceLocation(Lock.MOD_ID, "add_lockable");
    public static final ResourceLocation ADD_LOCKABLE_CHUNK = new ResourceLocation(Lock.MOD_ID, "add_lockable_chunk");
    public static final ResourceLocation REMOVE_LOCKABLE = new ResourceLocation(Lock.MOD_ID, "remove_lockable");
    public static final ResourceLocation UPDATE_LOCKABLE = new ResourceLocation(Lock.MOD_ID, "update_lockable");
    public static final ResourceLocation TRY_PIN = new ResourceLocation(Lock.MOD_ID, "try_pin");
    public static final ResourceLocation TRY_PIN_RESULT = new ResourceLocation(Lock.MOD_ID, "try_pin_result");

    private LockNetwork() {
    }

    public static void register() {

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, ADD_LOCKABLE, AddLockablePacket::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, ADD_LOCKABLE_CHUNK, AddLockableToChunkPacket::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, REMOVE_LOCKABLE, RemoveLockablePacket::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, UPDATE_LOCKABLE, UpdateLockablePacket::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, TRY_PIN_RESULT, TryPinResultPacket::handle);

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, TRY_PIN, TryPinPacket::handle);
    }

    
    public static void sendToPlayer(ServerPlayer player, ResourceLocation id, FriendlyByteBuf buf) {
        NetworkManager.sendToPlayer(player, id, buf);
    }

    
    public static void sendToServer(ResourceLocation id, FriendlyByteBuf buf) {
        NetworkManager.sendToServer(id, buf);
    }

    
    public static FriendlyByteBuf createBuf() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }
}
