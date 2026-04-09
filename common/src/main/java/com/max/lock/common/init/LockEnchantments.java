package com.max.lock.common.init;

import com.max.lock.common.Lock;
import com.max.lock.common.item.LockItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * 模组附魔注册（3个附魔）
 * 使用 EnchantmentCategory.WEAPON 作为基础分类，通过 canEnchant 覆盖实现锁专属过滤
 */
public final class LockEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(Lock.MOD_ID,
            Registries.ENCHANTMENT);

    private static final EquipmentSlot[] NONE = new EquipmentSlot[] {};

    /** 电击附魔 — 撬锁失败时触发电击 */
    public static final RegistrySupplier<Enchantment> SHOCKING = ENCHANTMENTS.register("shocking",
            () -> new LockEnchantment(Enchantment.Rarity.RARE, 3));

    /** 坚固附魔 — 增加锁的抵抗力 */
    public static final RegistrySupplier<Enchantment> STURDY = ENCHANTMENTS.register("sturdy",
            () -> new LockEnchantment(Enchantment.Rarity.UNCOMMON, 3));

    /** 复杂度附魔 — 降低被撬锁的概率 */
    public static final RegistrySupplier<Enchantment> COMPLEXITY = ENCHANTMENTS.register("complexity",
            () -> new LockEnchantment(Enchantment.Rarity.UNCOMMON, 3));

    private LockEnchantments() {
    }

    public static void register() {
        ENCHANTMENTS.register();
    }

    /** 锁专属附魔基类 */
    private static class LockEnchantment extends Enchantment {
        private final int maxLevel;

        protected LockEnchantment(Rarity rarity, int maxLevel) {
            super(rarity, EnchantmentCategory.WEAPON, NONE);
            this.maxLevel = maxLevel;
        }

        @Override
        public int getMaxLevel() {
            return this.maxLevel;
        }

        @Override
        public boolean canEnchant(ItemStack stack) {
            return stack.getItem() instanceof LockItem;
        }
    }
}
