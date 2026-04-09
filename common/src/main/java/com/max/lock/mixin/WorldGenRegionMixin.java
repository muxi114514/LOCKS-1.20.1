package com.max.lock.mixin;

import com.max.lock.common.util.LocksUtil;
import com.max.lock.common.util.WorldGenContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;







@Mixin(WorldGenRegion.class)
public class WorldGenRegionMixin {
    @Shadow
    @Final
    private ServerLevel level;

    @Unique
    private final RandomSource locks$rng = RandomSource.create();

    @Inject(method = "setBlock", at = @At(value = "RETURN", ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void locks$lockBlock(BlockPos blockPos, BlockState state, int flags, int recursionLeft,
            CallbackInfoReturnable<Boolean> cir, ChunkAccess chunkAccess, BlockState oldState) {

        WorldGenContext.CURRENT_REGION.set((WorldGenRegion) (Object) this);


        if (!state.hasBlockEntity())
            return;
        BlockEntity be = ((LevelAccessor) (Object) this).getBlockEntity(blockPos);
        if (!(be instanceof RandomizableContainerBlockEntity))
            return;
        LocksUtil.lockChunk((LevelAccessor) (Object) this, this.level, blockPos,
                this.locks$rng, chunkAccess);
    }
}
