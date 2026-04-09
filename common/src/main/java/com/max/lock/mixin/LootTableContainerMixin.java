package com.max.lock.mixin;

import com.max.lock.common.config.LocksConfig;
import com.max.lock.common.util.LocksUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 拦截容器设置战利品表，根据配置的战利品表规则自动加锁
 *
 * 覆盖两条路径：
 * 1. 静态 setLootTable —— 末地城/地牢/要塞等旧式 StructurePiece 结构
 * 2. tryLoadLootTable —— 村庄等 Jigsaw 模板结构（通过 NBT 反序列化设置战利品表）
 */
@Mixin(RandomizableContainerBlockEntity.class)
public abstract class LootTableContainerMixin {

    @Shadow
    protected ResourceLocation lootTable;

    @Unique
    private static final RandomSource locks$rng = RandomSource.create();

    // ===== 路径 1: 旧式结构的静态方法 =====

    @Inject(method = "setLootTable(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Lnet/minecraft/resources/ResourceLocation;)V",
            at = @At("TAIL"))
    private static void locks$onSetLootTable(BlockGetter level, RandomSource random,
            BlockPos pos, ResourceLocation lootTable, CallbackInfo ci) {
        if (!(level instanceof WorldGenRegion region))
            return;
        if (lootTable == null)
            return;

        LocksConfig.LootTableRule rule = LocksConfig.findLootTableRule(lootTable);
        if (rule == null)
            return;
        if (!LocksUtil.chance(locks$rng, rule.chance))
            return;

        ChunkAccess chunk = region.getChunk(pos);
        LocksUtil.lockChunkByLootTable(region, pos, locks$rng, chunk, rule);
    }

    // ===== 路径 2: Jigsaw 模板结构的 NBT 加载 =====

    @Inject(method = "tryLoadLootTable", at = @At("RETURN"))
    private void locks$onTryLoadLootTable(CompoundTag tag, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue())
            return;
        if (this.lootTable == null)
            return;

        WorldGenRegion region = com.max.lock.common.util.WorldGenContext.CURRENT_REGION.get();
        if (region == null)
            return;

        LocksConfig.LootTableRule rule = LocksConfig.findLootTableRule(this.lootTable);
        if (rule == null)
            return;
        if (!LocksUtil.chance(locks$rng, rule.chance))
            return;

        BlockPos pos = ((BlockEntity) (Object) this).getBlockPos();
        ChunkAccess chunk = region.getChunk(pos);
        LocksUtil.lockChunkByLootTable(region, pos, locks$rng, chunk, rule);
    }
}
