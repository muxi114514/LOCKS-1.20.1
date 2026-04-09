package com.max.lock.common.util;

import java.util.Random;
import java.util.stream.Stream;

import com.max.lock.common.capability.ILockableHandler;
import com.max.lock.common.capability.LockCapabilityAccess;
import com.max.lock.common.config.LocksConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.*;
import static net.minecraft.world.level.block.state.properties.DoorHingeSide.LEFT;
import static net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER;

/**
 * 通用工具方法集合
 */
public final class LocksUtil {

    private LocksUtil() {
    }

    /** Fisher-Yates 洗牌算法 */
    public static void shuffle(byte[] array, Random rng) {
        for (int a = array.length - 1; a > 0; --a) {
            int index = rng.nextInt(a + 1);
            byte temp = array[index];
            array[index] = array[a];
            array[a] = temp;
        }
    }

    /** 概率判定 (Random) */
    public static boolean chance(Random rng, double ch) {
        return ch == 1d || ch != 0d && rng.nextDouble() <= ch;
    }

    /** 概率判定 (RandomSource) */
    public static boolean chance(RandomSource rng, double ch) {
        return ch == 1d || ch != 0d && rng.nextDouble() <= ch;
    }

    /** Direction 转 AttachFace */
    public static AttachFace faceFromDir(Direction dir) {
        return dir == Direction.UP ? AttachFace.CEILING : dir == Direction.DOWN ? AttachFace.FLOOR : AttachFace.WALL;
    }

    /** AABB 绕 Y 轴旋转 90° */
    public static AABB rotateY(AABB bb) {
        return new AABB(bb.minZ, bb.minY, bb.minX, bb.maxZ, bb.maxY, bb.maxX);
    }

    /** AABB 绕 X 轴旋转 90° */
    public static AABB rotateX(AABB bb) {
        return new AABB(bb.minX, bb.minZ, bb.minY, bb.maxX, bb.maxZ, bb.maxY);
    }

    /** 包含边界的相交检测 */
    public static boolean intersectsInclusive(AABB bb1, AABB bb2) {
        return bb1.minX <= bb2.maxX && bb1.maxX >= bb2.minX
                && bb1.minY <= bb2.maxY && bb1.maxY >= bb2.minY
                && bb1.minZ <= bb2.maxZ && bb1.maxZ >= bb2.minZ;
    }

    /** AABB 指定面的中心点 */
    public static Vec3 sideCenter(AABB bb, Direction side) {
        Vec3i dir = side.getNormal();
        return new Vec3(
                (bb.minX + bb.maxX + (bb.maxX - bb.minX) * dir.getX()) * 0.5d,
                (bb.minY + bb.maxY + (bb.maxY - bb.minY) * dir.getY()) * 0.5d,
                (bb.minZ + bb.maxZ + (bb.maxZ - bb.minZ) * dir.getZ()) * 0.5d);
    }

    /** 获取指定位置上所有相交的 Lockable */
    public static Stream<Lockable> intersecting(Level world, BlockPos pos) {
        ILockableHandler handler = LockCapabilityAccess.getHandler(world);
        if (handler == null)
            return Stream.empty();
        return handler.getInChunk(pos).values().stream().filter(lkb -> lkb.bb.intersects(pos));
    }

    /** 判断指定位置是否有被上锁的方块 */
    public static boolean locked(Level world, BlockPos pos) {
        return intersecting(world, pos).anyMatch(LocksPredicates.LOCKED);
    }

    // ===== 世界生成锁 =====

    /**
     * 世界生成时为容器方块创建 Lockable
     * 根据方块朝向和类型（箱子/门等）确定锁的位置和范围
     */
    public static Lockable lockWhenGen(LevelAccessor levelAccessor, ServerLevel level,
            BlockPos blockPos, RandomSource rng) {
        BlockState state = levelAccessor.getBlockState(blockPos);
        if (!LocksConfig.canGen(rng, state.getBlock()))
            return null;

        BlockPos pos1 = blockPos;
        Direction dir;
        if (state.hasProperty(FACING)) {
            dir = state.getValue(FACING);
        } else if (state.hasProperty(HORIZONTAL_FACING)) {
            dir = state.getValue(HORIZONTAL_FACING);
        } else {
            dir = Direction.DOWN;
        }

        // 双箱子：只锁左半边（右半边跳过）
        if (state.hasProperty(CHEST_TYPE)) {
            switch (state.getValue(CHEST_TYPE)) {
                case LEFT -> pos1 = blockPos.relative(ChestBlock.getConnectedDirection(state));
                case RIGHT -> {
                    return null;
                }
                default -> {
                    /* SINGLE: 单箱子正常加锁 */ }
            }
        }

        // 双高方块（门）
        if (state.hasProperty(DOUBLE_BLOCK_HALF)) {
            if (state.getValue(DOUBLE_BLOCK_HALF) == LOWER)
                return null;
            pos1 = blockPos.below();
            if (state.hasProperty(DOOR_HINGE)) {
                if (state.hasProperty(HORIZONTAL_FACING)) {
                    BlockPos pos2 = pos1.relative(
                            state.getValue(DOOR_HINGE) == LEFT ? dir.getClockWise() : dir.getCounterClockWise());
                    if (levelAccessor.getBlockState(pos2).is(state.getBlock())) {
                        if (state.getValue(DOOR_HINGE) == LEFT)
                            return null;
                        pos1 = pos2;
                    }
                }
                dir = dir.getOpposite();
            }
        }

        Cuboid6i bb = new Cuboid6i(blockPos, pos1);
        ItemStack stack = LocksConfig.getRandomLock(rng);
        LockData lock = com.max.lock.common.item.LockItem.createLock(stack);
        Transform tr = Transform.fromDirection(dir, dir);
        if (tr == null)
            tr = Transform.NORTH_UP;
        return new Lockable(bb, lock, tr, stack, rng.nextInt(Integer.MAX_VALUE));
    }

    /** 在世界生成时锁住方块并缓存到 ProtoChunk */
    public static boolean lockChunk(LevelAccessor levelAccessor, ServerLevel level,
            BlockPos blockPos, RandomSource rng, ChunkAccess chunkAccess) {
        if (!levelAccessor.hasChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4))
            return false;
        Lockable lkb = lockWhenGen(levelAccessor, level, blockPos, rng);
        if (lkb == null)
            return false;
        ((ILockableProvider) chunkAccess).getLockables().add(lkb);
        return true;
    }

    // ===== 战利品表驱动的世界生成锁 =====

    /** 检查 ProtoChunk 中是否已有覆盖该位置的 Lockable（防重复加锁） */
    public static boolean hasLockableAt(ChunkAccess chunkAccess, BlockPos pos) {
        if (!(chunkAccess instanceof ILockableProvider provider))
            return false;
        for (Lockable lkb : provider.getLockables()) {
            if (lkb.bb.intersects(pos))
                return true;
        }
        return false;
    }

    /**
     * 根据方块状态计算锁的朝向和范围，创建 Lockable
     * 从 lockWhenGen 中提取的公共逻辑，供不同加锁路径复用
     * 
     * @param stack 锁物品
     * @return Lockable 或 null（如双箱子右半边等不适合加锁的情况）
     */
    public static Lockable buildLockable(LevelAccessor levelAccessor, BlockPos blockPos,
            RandomSource rng, ItemStack stack) {
        BlockState state = levelAccessor.getBlockState(blockPos);
        BlockPos pos1 = blockPos;
        Direction dir;
        if (state.hasProperty(FACING)) {
            dir = state.getValue(FACING);
        } else if (state.hasProperty(HORIZONTAL_FACING)) {
            dir = state.getValue(HORIZONTAL_FACING);
        } else {
            dir = Direction.DOWN;
        }

        // 双箱子：只锁左半边
        if (state.hasProperty(CHEST_TYPE)) {
            switch (state.getValue(CHEST_TYPE)) {
                case LEFT -> pos1 = blockPos.relative(ChestBlock.getConnectedDirection(state));
                case RIGHT -> {
                    return null;
                }
                default -> { /* SINGLE */ }
            }
        }

        // 双高方块（门）
        if (state.hasProperty(DOUBLE_BLOCK_HALF)) {
            if (state.getValue(DOUBLE_BLOCK_HALF) == LOWER)
                return null;
            pos1 = blockPos.below();
            if (state.hasProperty(DOOR_HINGE)) {
                if (state.hasProperty(HORIZONTAL_FACING)) {
                    BlockPos pos2 = pos1.relative(
                            state.getValue(DOOR_HINGE) == LEFT ? dir.getClockWise() : dir.getCounterClockWise());
                    if (levelAccessor.getBlockState(pos2).is(state.getBlock())) {
                        if (state.getValue(DOOR_HINGE) == LEFT)
                            return null;
                        pos1 = pos2;
                    }
                }
                dir = dir.getOpposite();
            }
        }

        Cuboid6i bb = new Cuboid6i(blockPos, pos1);
        LockData lock = com.max.lock.common.item.LockItem.createLock(stack);
        Transform tr = Transform.fromDirection(dir, dir);
        if (tr == null)
            tr = Transform.NORTH_UP;
        return new Lockable(bb, lock, tr, stack, rng.nextInt(Integer.MAX_VALUE));
    }

    /**
     * 基于战利品表规则为容器加锁（由 LootTableContainerMixin 调用）
     * 使用规则独立锁列表或全局锁列表
     */
    public static boolean lockChunkByLootTable(LevelAccessor levelAccessor,
            BlockPos blockPos, RandomSource rng, ChunkAccess chunkAccess,
            LocksConfig.LootTableRule rule) {
        // 防重复：已被全局规则锁住则跳过
        if (hasLockableAt(chunkAccess, blockPos))
            return false;
        ItemStack stack = LocksConfig.getRandomLockForRule(rng, rule);
        Lockable lkb = buildLockable(levelAccessor, blockPos, rng, stack);
        if (lkb == null)
            return false;
        ((ILockableProvider) chunkAccess).getLockables().add(lkb);
        return true;
    }
}
