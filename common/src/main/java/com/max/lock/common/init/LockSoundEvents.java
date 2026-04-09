package com.max.lock.common.init;

import com.max.lock.common.Lock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/**
 * 模组音效注册
 */
public final class LockSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Lock.MOD_ID,
            Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> KEY_RING = add("key_ring"),
            LOCK_CLOSE = add("lock.close"),
            LOCK_OPEN = add("lock.open"),
            LOCK_RATTLE = add("lock.rattle"),
            PIN_FAIL = add("pin.fail"),
            PIN_MATCH = add("pin.match"),
            SHOCK = add("shock");

    private LockSoundEvents() {
    }

    public static void register() {
        SOUND_EVENTS.register();
    }

    private static RegistrySupplier<SoundEvent> add(String name) {
        ResourceLocation id = new ResourceLocation(Lock.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
