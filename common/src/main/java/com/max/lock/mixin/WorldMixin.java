package com.max.lock.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.max.lock.common.util.LocksUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * 红石信号屏蔽：锁住的方块不响应红石信号
 * hasNeighborSignal 定义在 SignalGetter 接口中，Level 继承但未 override，
 * 因此使用 remap=false 并直接指定 SRG 名以绕过 Loom 的错误映射
 */
@Mixin(Level.class)
public class WorldMixin {
    @Inject(at = @At("HEAD"), method = "m_276867_", cancellable = true, remap = false)
    private void locks$hasNeighborSignal(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (LocksUtil.locked((Level) (Object) this, pos))
            cir.setReturnValue(false);
    }
}
