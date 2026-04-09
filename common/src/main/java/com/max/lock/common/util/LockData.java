package com.max.lock.common.util;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;





public class LockData {
    public final int id;

    protected final byte[] combo;
    protected boolean locked;
    public final Random rng;

    private final List<LockableListener> listeners = new CopyOnWriteArrayList<>();

    public LockData(int id, int length, boolean locked) {
        this.id = id;
        this.rng = new Random(id);
        this.combo = this.shuffle(length);
        this.locked = locked;
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



    private static final String KEY_ID = "Id", KEY_LENGTH = "Length", KEY_LOCKED = "Locked";

    public static LockData fromNbt(CompoundTag nbt) {
        return new LockData(nbt.getInt(KEY_ID), nbt.getByte(KEY_LENGTH), nbt.getBoolean(KEY_LOCKED));
    }

    public static CompoundTag toNbt(LockData lock) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(KEY_ID, lock.id);
        nbt.putByte(KEY_LENGTH, (byte) lock.combo.length);
        nbt.putBoolean(KEY_LOCKED, lock.locked);
        return nbt;
    }



    public static LockData fromBuf(FriendlyByteBuf buf) {
        return new LockData(buf.readInt(), buf.readByte(), buf.readBoolean());
    }

    public static void toBuf(FriendlyByteBuf buf, LockData lock) {
        buf.writeInt(lock.id);
        buf.writeByte(lock.getLength());
        buf.writeBoolean(lock.isLocked());
    }



    public byte[] shuffle(int length) {
        byte[] combo = new byte[length];
        for (byte a = 0; a < length; ++a)
            combo[a] = a;
        LocksUtil.shuffle(combo, this.rng);
        return combo;
    }

    public int getLength() {
        return this.combo.length;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean locked) {
        if (this.locked == locked)
            return;
        this.locked = locked;
        this.notifyListeners();
    }

    public int getPin(int index) {
        return this.combo[index];
    }

    public boolean checkPin(int index, int pin) {
        return this.getPin(index) == pin;
    }
}
