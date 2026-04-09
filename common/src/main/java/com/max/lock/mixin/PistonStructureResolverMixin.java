package com.max.lock.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.max.lock.common.util.LocksUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonStructureResolver;




@Mixin(PistonStructureResolver.class)
public class PistonStructureResolverMixin {
    @Shadow
    private Level level;
    @Shadow
    private BlockPos startPos;

    @Inject(at = @At("HEAD"), method = "resolve()Z", cancellable = true)
    private void locks$resolve(CallbackInfoReturnable<Boolean> cir) {
        if (LocksUtil.locked(this.level, this.startPos))
            cir.setReturnValue(false);
    }
}
