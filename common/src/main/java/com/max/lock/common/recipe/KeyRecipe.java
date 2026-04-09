package com.max.lock.common.recipe;

import com.max.lock.common.init.LockItems;
import com.max.lock.common.init.LockRecipeSerializers;
import com.max.lock.common.item.LockingItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * 钥匙合成配方：锁/钥匙（含ID）+ 空白钥匙 → 专用钥匙
 * 锁在合成后保留不消耗
 */
public class KeyRecipe extends CustomRecipe {
    public KeyRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return LockRecipeSerializers.KEY.get();
    }

    @Override
    public boolean matches(CraftingContainer inv, Level world) {
        boolean hasLocking = false;
        int blanks = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty())
                continue;
            if (stack.hasTag() && stack.getTag().contains(LockingItem.KEY_ID)) {
                if (hasLocking)
                    return false;
                hasLocking = true;
            } else if (stack.getItem() == LockItems.KEY_BLANK.get()) {
                blanks++;
            } else {
                return false;
            }
        }
        return hasLocking && blanks >= 1;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack locking = ItemStack.EMPTY;
        int blanks = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty())
                continue;
            if (stack.hasTag() && stack.getTag().contains(LockingItem.KEY_ID)) {
                if (!locking.isEmpty())
                    return ItemStack.EMPTY;
                locking = stack;
            } else if (stack.getItem() == LockItems.KEY_BLANK.get()) {
                blanks++;
            } else {
                return ItemStack.EMPTY;
            }
        }
        if (!locking.isEmpty() && blanks >= 1)
            return LockingItem.copyId(locking, new ItemStack(LockItems.KEY.get()));
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> list = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < list.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.hasTag() || !stack.getTag().contains(LockingItem.KEY_ID))
                continue;
            // 保留带ID的物品（锁/钥匙）不消耗
            list.set(i, stack.copy());
            break;
        }
        return list;
    }

    @Override
    public boolean canCraftInDimensions(int x, int y) {
        return x >= 2 && y >= 1;
    }
}
