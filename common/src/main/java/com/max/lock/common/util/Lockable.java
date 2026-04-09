package com.max.lock.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;





public class Lockable implements LockableListener {

    


    public static class State {
        public static final AABB VERT_Z_BB = new AABB(-2d / 16d, -3d / 16d, 0.5d / 16d, 2d / 16d, 3d / 16d, 0.5d / 16d),
                VERT_X_BB = LocksUtil.rotateY(VERT_Z_BB),
                HOR_Z_BB = LocksUtil.rotateX(VERT_Z_BB),
                HOR_X_BB = LocksUtil.rotateY(HOR_Z_BB);

        public static AABB getBounds(Transform tr) {
            return tr.face == AttachFace.WALL
                    ? tr.dir.getAxis() == Direction.Axis.Z ? VERT_Z_BB : VERT_X_BB
                    : tr.dir.getAxis() == Direction.Axis.Z ? HOR_Z_BB : HOR_X_BB;
        }

        public final Vec3 pos;
        public final Transform tr;
        public final AABB bb;

        public State(Vec3 pos, Transform tr) {
            this(pos, tr, getBounds(tr).move(pos));
        }

        public State(Vec3 pos, Transform tr, AABB bb) {
            this.pos = pos;
            this.tr = tr;
            this.bb = bb;
        }
    }

    public final Cuboid6i bb;
    public final LockData lock;
    public final Transform tr;
    public final ItemStack stack;
    public final int id;

    public int oldSwingTicks, swingTicks, maxSwingTicks;

    public Map<List<BlockState>, State> cache = new HashMap<>(6);

    private final List<LockableListener> listeners = new CopyOnWriteArrayList<>();

    public Lockable(Cuboid6i bb, LockData lock, Transform tr, ItemStack stack, int id) {
        this.bb = bb;
        this.lock = lock;
        this.tr = tr;
        this.stack = stack;
        this.id = id;
        lock.addListener(this);
    }



    public void addListener(LockableListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(LockableListener listener) {
        this.listeners.remove(listener);
    }

    protected void notifyListeners() {
        for (LockableListener listener : this.listeners) {
            listener.onChanged(this);
        }
    }

    @Override
    public void onChanged(Object source) {
        this.notifyListeners();

        com.max.lock.common.item.LockItem.setOpen(this.stack, !this.lock.isLocked());
    }



    public static final String KEY_BB = "Bb", KEY_LOCK = "Lock", KEY_TRANSFORM = "Transform", KEY_STACK = "Stack",
            KEY_ID = "Id";

    public static Lockable fromNbt(CompoundTag nbt) {
        return new Lockable(
                Cuboid6i.fromNbt(nbt.getCompound(KEY_BB)),
                LockData.fromNbt(nbt.getCompound(KEY_LOCK)),
                Transform.values()[(int) nbt.getByte(KEY_TRANSFORM)],
                ItemStack.of(nbt.getCompound(KEY_STACK)),
                nbt.getInt(KEY_ID));
    }

    public static CompoundTag toNbt(Lockable lkb) {
        CompoundTag nbt = new CompoundTag();
        nbt.put(KEY_BB, Cuboid6i.toNbt(lkb.bb));
        nbt.put(KEY_LOCK, LockData.toNbt(lkb.lock));
        nbt.putByte(KEY_TRANSFORM, (byte) lkb.tr.ordinal());
        CompoundTag stackTag = new CompoundTag();
        lkb.stack.save(stackTag);
        nbt.put(KEY_STACK, stackTag);
        nbt.putInt(KEY_ID, lkb.id);
        return nbt;
    }

    public static int idFromNbt(CompoundTag nbt) {
        return nbt.getInt(KEY_ID);
    }



    public static Lockable fromBuf(FriendlyByteBuf buf) {
        return new Lockable(
                Cuboid6i.fromBuf(buf),
                LockData.fromBuf(buf),
                buf.readEnum(Transform.class),
                buf.readItem(),
                buf.readInt());
    }

    public static void toBuf(FriendlyByteBuf buf, Lockable lkb) {
        Cuboid6i.toBuf(buf, lkb.bb);
        LockData.toBuf(buf, lkb.lock);
        buf.writeEnum(lkb.tr);
        buf.writeItem(lkb.stack);
        buf.writeInt(lkb.id);
    }



    public void tick() {
        this.oldSwingTicks = this.swingTicks;
        if (this.swingTicks > 0)
            --this.swingTicks;
    }

    public void swing(int ticks) {
        this.swingTicks = this.oldSwingTicks = this.maxSwingTicks = ticks;
    }



    


    @SuppressWarnings("deprecation")
    public State getLockState(Level world) {
        List<BlockState> states = new ArrayList<>(this.bb.volume());
        for (BlockPos pos : this.bb.getContainedPos()) {
            if (!world.hasChunkAt(pos))
                return null;
            states.add(world.getBlockState(pos));
        }
        State state = this.cache.get(states);
        if (state != null)
            return state;

        ArrayList<AABB> boxes = new ArrayList<>(4);
        for (BlockPos pos : this.bb.getContainedPos()) {
            VoxelShape shape = world.getBlockState(pos).getShape(world, pos);
            if (shape.isEmpty())
                continue;
            AABB shapeBB = shape.bounds().move(pos);
            AABB union = shapeBB;
            Iterator<AABB> it = boxes.iterator();
            while (it.hasNext()) {
                AABB bb1 = it.next();
                if (LocksUtil.intersectsInclusive(union, bb1)) {
                    union = union.minmax(bb1);
                    it.remove();
                }
            }
            boxes.add(union);
        }
        if (boxes.isEmpty())
            return null;

        Direction side = this.tr.getCuboidFace();
        Vec3 center = this.bb.sideCenter(side);
        Vec3 point = center;
        double min = -1d;
        for (AABB box : boxes) {
            for (Direction side1 : Direction.values()) {
                Vec3 point1 = LocksUtil.sideCenter(box, side1)
                        .add(Vec3.atLowerCornerOf(side1.getNormal()).scale(0.05d));
                double dist = center.distanceToSqr(point1);
                if (min != -1d && dist >= min)
                    continue;
                point = point1;
                min = dist;
                side = side1;
            }
        }
        state = new State(point, Transform.fromDirection(side, this.tr.dir));
        this.cache.put(states, state);
        return state;
    }
}
