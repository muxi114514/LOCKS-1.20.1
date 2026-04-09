package com.max.lock.common.init;

import com.max.lock.common.Lock;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * 创造模式物品栏标签页
 */
public final class LockCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Lock.MOD_ID,
            Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> LOCKS_TAB = TABS.register("locks_tab",
            () -> CreativeTabRegistry.create(
                    builder -> builder
                            .title(Component.translatable("itemGroup." + Lock.MOD_ID))
                            .icon(() -> new ItemStack(LockItems.IRON_LOCK.get()))
                            .displayItems((params, output) -> {
                                // 材料
                                output.accept(LockItems.SPRING.get());
                                output.accept(LockItems.WOOD_LOCK_MECHANISM.get());
                                output.accept(LockItems.IRON_LOCK_MECHANISM.get());
                                output.accept(LockItems.STEEL_LOCK_MECHANISM.get());
                                output.accept(LockItems.KEY_BLANK.get());
                                // 锁
                                output.accept(LockItems.WOOD_LOCK.get());
                                output.accept(LockItems.IRON_LOCK.get());
                                output.accept(LockItems.STEEL_LOCK.get());
                                output.accept(LockItems.GOLD_LOCK.get());
                                output.accept(LockItems.DIAMOND_LOCK.get());
                                output.accept(LockItems.NETHERITE_LOCK.get());
                                // 钥匙
                                output.accept(LockItems.KEY.get());
                                output.accept(LockItems.MASTER_KEY.get());
                                // 钥匙圈
                                output.accept(LockItems.KEY_RING.get());
                                // 撬锁工具
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
