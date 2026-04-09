package com.max.lock.common.init;

import com.max.lock.common.Lock;
import com.max.lock.common.item.KeyItem;
import com.max.lock.common.item.KeyRingItem;
import com.max.lock.common.item.LockItem;
import com.max.lock.common.item.LockPickItem;
import com.max.lock.common.item.MasterKeyItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;




public final class LockItems {
        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Lock.MOD_ID, Registries.ITEM);


        public static final RegistrySupplier<Item> SPRING = ITEMS.register("spring",
                        () -> new Item(new Item.Properties())),
                        WOOD_LOCK_MECHANISM = ITEMS.register("wood_lock_mechanism",
                                        () -> new Item(new Item.Properties())),
                        IRON_LOCK_MECHANISM = ITEMS.register("iron_lock_mechanism",
                                        () -> new Item(new Item.Properties())),
                        STEEL_LOCK_MECHANISM = ITEMS.register("steel_lock_mechanism",
                                        () -> new Item(new Item.Properties())),
                        KEY_BLANK = ITEMS.register("key_blank", () -> new Item(new Item.Properties()));


        public static final RegistrySupplier<Item> WOOD_LOCK = ITEMS.register("wood_lock",
                        () -> new LockItem(5, 15, 4, new Item.Properties())),
                        IRON_LOCK = ITEMS.register("iron_lock", () -> new LockItem(7, 14, 12, new Item.Properties())),
                        STEEL_LOCK = ITEMS.register("steel_lock", () -> new LockItem(9, 12, 20, new Item.Properties())),
                        GOLD_LOCK = ITEMS.register("gold_lock", () -> new LockItem(6, 22, 6, new Item.Properties())),
                        DIAMOND_LOCK = ITEMS.register("diamond_lock",
                                        () -> new LockItem(11, 10, 100, new Item.Properties())),
                        NETHERITE_LOCK = ITEMS.register("netherite_lock",
                                        () -> new LockItem(13, 15, 200, new Item.Properties()));


        public static final RegistrySupplier<Item> KEY = ITEMS.register("key",
                        () -> new KeyItem(new Item.Properties())),
                        MASTER_KEY = ITEMS.register("master_key", () -> new MasterKeyItem(new Item.Properties()));


        public static final RegistrySupplier<Item> KEY_RING = ITEMS.register("key_ring",
                        () -> new KeyRingItem(1, new Item.Properties()));


        public static final RegistrySupplier<Item> WOOD_LOCK_PICK = ITEMS.register("wood_lock_pick",
                        () -> new LockPickItem(0.20f, Integer.MAX_VALUE, false, new Item.Properties())),
                        IRON_LOCK_PICK = ITEMS.register("iron_lock_pick",
                                        () -> new LockPickItem(0.30f, Integer.MAX_VALUE, false, new Item.Properties())),
                        STEEL_LOCK_PICK = ITEMS.register("steel_lock_pick",
                                        () -> new LockPickItem(0.65f, 3, false, new Item.Properties())),
                        GOLD_LOCK_PICK = ITEMS.register("gold_lock_pick",
                                        () -> new LockPickItem(0.55f, 2, true, new Item.Properties())),
                        DIAMOND_LOCK_PICK = ITEMS.register("diamond_lock_pick",
                                        () -> new LockPickItem(0.85f, 1, false, new Item.Properties())),
                        NETHERITE_LOCK_PICK = ITEMS.register("netherite_lock_pick",
                                        () -> new LockPickItem(2.00f, 0, true, new Item.Properties()));

        private LockItems() {
        }

        public static void register() {
                ITEMS.register();
        }
}
