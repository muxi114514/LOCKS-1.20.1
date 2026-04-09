package com.max.lock.mixin;

import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.max.lock.common.capability.ILockableHandler;
import com.max.lock.common.capability.LockCapabilityAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.BlockState;




@Mixin(ServerLevel.class)
public class ServerWorldMixin {
    @Inject(at = @At("HEAD"), method = "sendBlockUpdated(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;I)V")
    private void locks$sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flag,
            CallbackInfo ci) {
        if (oldState.is(newState.getBlock()))
            return;
        ServerLevel world = (ServerLevel) (Object) this;
        ILockableHandler handler = LockCapabilityAccess.getHandler(world);
        if (handler == null)
            return;
        handler.getInChunk(pos).values().stream()
                .filter(lkb -> lkb.bb.intersects(pos))
                .collect(Collectors.toList())
                .forEach(lkb -> {
                    world.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.8f,
                            0.8f + world.random.nextFloat() * 0.4f);
                    world.addFreshEntity(
                            new ItemEntity(world, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, lkb.stack));
                    handler.remove(lkb.id);
                });
    }
}
