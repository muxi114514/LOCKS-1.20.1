package com.max.lock.common.init;

import com.max.lock.common.Lock;
import com.max.lock.common.menu.KeyRingMenu;
import com.max.lock.common.menu.LockPickingMenu;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

/**
 * 菜单类型注册
 */
public final class LockMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Lock.MOD_ID, Registries.MENU);

    @SuppressWarnings("unchecked")
    public static final RegistrySupplier<MenuType<LockPickingMenu>> LOCK_PICKING = (RegistrySupplier<MenuType<LockPickingMenu>>) (RegistrySupplier<?>) MENUS
            .register("lock_picking",
                    () -> MenuRegistry.ofExtended(LockPickingMenu::fromNetwork));

    @SuppressWarnings("unchecked")
    public static final RegistrySupplier<MenuType<KeyRingMenu>> KEY_RING = (RegistrySupplier<MenuType<KeyRingMenu>>) (RegistrySupplier<?>) MENUS
            .register("key_ring",
                    () -> MenuRegistry.ofExtended(KeyRingMenu::fromNetwork));

    private LockMenuTypes() {
    }

    public static void register() {
        MENUS.register();
    }
}
