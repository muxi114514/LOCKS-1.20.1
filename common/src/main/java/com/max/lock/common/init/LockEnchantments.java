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





public final class LockEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(Lock.MOD_ID,
            Registries.ENCHANTMENT);

    private static final EquipmentSlot[] NONE = new EquipmentSlot[] {};

    
    public static final RegistrySupplier<Enchantment> SHOCKING = ENCHANTMENTS.register("shocking",
            () -> new LockEnchantment(Enchantment.Rarity.RARE, 3));

    
    public static final RegistrySupplier<Enchantment> STURDY = ENCHANTMENTS.register("sturdy",
            () -> new LockEnchantment(Enchantment.Rarity.UNCOMMON, 3));

    
    public static final RegistrySupplier<Enchantment> COMPLEXITY = ENCHANTMENTS.register("complexity",
            () -> new LockEnchantment(Enchantment.Rarity.UNCOMMON, 3));

    private LockEnchantments() {
    }

    public static void register() {
        ENCHANTMENTS.register();
    }

    
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
