package com.max.lock.common.capability.forge;

import com.max.lock.common.Lock;
import com.max.lock.common.capability.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;





public class LockCapabilityAccessImpl {
    public static final ResourceLocation HANDLER_ID = new ResourceLocation(Lock.MOD_ID, "lockable_handler");
    public static final ResourceLocation STORAGE_ID = new ResourceLocation(Lock.MOD_ID, "lockable_storage");
    public static final ResourceLocation SELECTION_ID = new ResourceLocation(Lock.MOD_ID, "selection");

    public static final Capability<ILockableHandler> LOCKABLE_HANDLER = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<ILockableStorage> LOCKABLE_STORAGE = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<ISelection> SELECTION = CapabilityManager.get(new CapabilityToken<>() {
    });



    @Nullable
    public static ILockableHandler getHandler(Level world) {
        return world.getCapability(LOCKABLE_HANDLER).orElse(null);
    }

    @Nullable
    public static ILockableStorage getStorage(LevelChunk chunk) {
        return chunk.getCapability(LOCKABLE_STORAGE).orElse(null);
    }

    @Nullable
    public static ISelection getSelection(Player player) {
        return player.getCapability(SELECTION).orElse(null);
    }



    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(ILockableHandler.class);
        event.register(ILockableStorage.class);
        event.register(ISelection.class);
    }



    public static void attachToWorld(AttachCapabilitiesEvent<Level> event) {
        Level world = event.getObject();
        LockableHandler handler = new LockableHandler(world);
        event.addCapability(HANDLER_ID, new SimpleCapProvider<>(LOCKABLE_HANDLER, handler));
    }

    public static void attachToChunk(AttachCapabilitiesEvent<LevelChunk> event) {
        LevelChunk chunk = event.getObject();
        LockableStorage storage = new LockableStorage(chunk);
        event.addCapability(STORAGE_ID, new SimpleCapProvider<>(LOCKABLE_STORAGE, storage));
    }

    public static void attachToEntity(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(SELECTION_ID, new SimpleCapProvider<>(SELECTION, new Selection()));
        }
    }
}
