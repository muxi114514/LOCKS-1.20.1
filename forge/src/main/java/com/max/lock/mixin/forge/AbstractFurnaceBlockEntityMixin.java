package com.max.lock.mixin.forge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.max.lock.common.util.LocksUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

// Furnace overrides getCapability separately
@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {
    @Inject(at = @At("HEAD"), method = "getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;", cancellable = true, remap = false)
    private void locks$getCapability(Capability<?> cap, Direction side, CallbackInfoReturnable<LazyOptional<?>> cir) {
        BlockEntity be = (BlockEntity) (Object) this;
        if (!be.isRemoved() && cap == ForgeCapabilities.ITEM_HANDLER && be.hasLevel()
                && LocksUtil.locked(be.getLevel(), be.getBlockPos()))
            cir.setReturnValue(LazyOptional.empty());
    }
}
