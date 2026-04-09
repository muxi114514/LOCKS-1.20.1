package com.max.lock.client.init;

import com.max.lock.client.gui.KeyRingScreen;
import com.max.lock.client.gui.LockPickingScreen;
import com.max.lock.common.init.LockMenuTypes;
import dev.architectury.registry.menu.MenuRegistry;

// Screen registration (called during client init)
public final class LockScreens {
    private LockScreens() {
    }

    public static void register() {
        MenuRegistry.registerScreenFactory(LockMenuTypes.LOCK_PICKING.get(), LockPickingScreen::new);
        MenuRegistry.registerScreenFactory(LockMenuTypes.KEY_RING.get(), KeyRingScreen::new);
    }
}
