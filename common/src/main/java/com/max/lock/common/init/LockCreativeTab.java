package com.max.lock.common.init;

import com.max.lock.common.Lock;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;




public final class LockCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Lock.MOD_ID,
            Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> LOCKS_TAB = TABS.register("locks_tab",
            () -> CreativeTabRegistry.create(
                    builder -> builder
                            .title(Component.translatable("itemGroup." + Lock.MOD_ID))
                            .icon(() -> new ItemStack(LockItems.IRON_LOCK.get()))
                            .displayItems((params, output) -> {

                                output.accept(LockItems.SPRING.get());
                                output.accept(LockItems.WOOD_LOCK_MECHANISM.get());
                                output.accept(LockItems.IRON_LOCK_MECHANISM.get());
                                output.accept(LockItems.STEEL_LOCK_MECHANISM.get());
                                output.accept(LockItems.KEY_BLANK.get());

                                output.accept(LockItems.WOOD_LOCK.get());
                                output.accept(LockItems.IRON_LOCK.get());
                                output.accept(LockItems.STEEL_LOCK.get());
                                output.accept(LockItems.GOLD_LOCK.get());
                                output.accept(LockItems.DIAMOND_LOCK.get());
                                output.accept(LockItems.NETHERITE_LOCK.get());

                                output.accept(LockItems.KEY.get());
                                output.accept(LockItems.MASTER_KEY.get());

                                output.accept(LockItems.KEY_RING.get());

                                output.accept(LockItems.WOOD_LOCK_PICK.get());
                                output.accept(LockItems.IRON_LOCK_PICK.get());
                                output.accept(LockItems.STEEL_LOCK_PICK.get());
                                output.accept(LockItems.GOLD_LOCK_PICK.get());
                                output.accept(LockItems.DIAMOND_LOCK_PICK.get());
                                output.accept(LockItems.NETHERITE_LOCK_PICK.get());
                            })));

    private LockCreativeTab() {
    }

    public static void register() {
        TABS.register();
    }
}
