package com.max.lock.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.max.lock.common.util.LocksUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

/**
 * 阻止漏斗从已上锁容器中吸取物品
 */
@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
    @Inject(at = @At("HEAD"), method = "getContainerAt(Lnet/minecraft/world/level/Level;DDD)Lnet/minecraft/world/Container;", cancellable = true)
    private static void locks$getContainerAt(Level world, double x, double y, double z,
            CallbackInfoReturnable<Container> cir) {
        if (LocksUtil.locked(world, BlockPos.containing(x, y, z)))
            cir.setReturnValue(null);
    }
}
