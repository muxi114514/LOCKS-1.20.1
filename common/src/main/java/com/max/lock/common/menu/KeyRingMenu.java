package com.max.lock.common.menu;

import com.max.lock.common.init.LockItemTags;
import com.max.lock.common.init.LockMenuTypes;
import com.max.lock.common.init.LockSoundEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;





public class KeyRingMenu extends AbstractContainerMenu {
    public final ItemStack ringStack;
    public final SimpleContainer keyInv;
    public final int rows;

    public KeyRingMenu(MenuType<?> type, int id, Player player, ItemStack ringStack, int rows) {
        super(type, id);
        this.ringStack = ringStack;
        this.rows = rows;
        this.keyInv = new SimpleContainer(rows * 9) {
            @Override
            public void setChanged() {
                super.setChanged();
                KeyRingMenu.saveToStack(ringStack, this);
            }
        };
        loadFromStack(ringStack, this.keyInv);


        for (int row = 0; row < rows; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new KeySlot(this.keyInv, player, col + row * 9, 8 + col * 18, 18 + row * 18));


        int offset = (rows - 4) * 18;
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(player.getInventory(), col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + offset));
        for (int col = 0; col < 9; ++col)
            this.addSlot(new Slot(player.getInventory(), col, 8 + col * 18, 161 + offset));
    }

    
    private static class KeySlot extends Slot {
        private final Player player;

        public KeySlot(SimpleContainer inv, Player player, int index, int x, int y) {
            super(inv, index, x, y);
            this.player = player;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(LockItemTags.KEYS);
        }

        @Override
        public void set(ItemStack stack) {
            super.set(stack);
            if (!player.level().isClientSide())
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        LockSoundEvents.KEY_RING.get(), SoundSource.PLAYERS, 1f, 1f);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return !this.ringStack.isEmpty();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem())
            return result;
        ItemStack slotStack = slot.getItem();
        result = slotStack.copy();
        int keySlots = this.rows * 9;
        if (index < keySlots) {
            if (!this.moveItemStackTo(slotStack, keySlots, this.slots.size(), true))
                return ItemStack.EMPTY;
        } else if (!this.moveItemStackTo(slotStack, 0, keySlots, false))
            return ItemStack.EMPTY;
        if (slotStack.isEmpty())
            slot.set(ItemStack.EMPTY);
        else
            slot.setChanged();
        return result;
    }



    private static void saveToStack(ItemStack ringStack, SimpleContainer inv) {
        var list = new net.minecraft.nbt.ListTag();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                var tag = new net.minecraft.nbt.CompoundTag();
                tag.putInt("Slot", i);
                stack.save(tag);
                list.add(tag);
            }
        }
        ringStack.getOrCreateTag().put("Items", list);
    }

    private static void loadFromStack(ItemStack ringStack, SimpleContainer inv) {
        if (!ringStack.hasTag() || !ringStack.getTag().contains("Items"))
            return;
        var list = ringStack.getTag().getList("Items", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            var tag = list.getCompound(i);
            int slot = tag.getInt("Slot");
            if (slot >= 0 && slot < inv.getContainerSize())
                inv.setItem(slot, ItemStack.of(tag));
        }
    }



    public static KeyRingMenu fromNetwork(int id, Inventory inv, FriendlyByteBuf buf) {
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        int rows = buf.readInt();
        return new KeyRingMenu(LockMenuTypes.KEY_RING.get(), id, inv.player, inv.player.getItemInHand(hand), rows);
    }

    public static class Provider implements MenuProvider {
        public final ItemStack stack;
        public final int rows;

        public Provider(ItemStack stack, int rows) {
            this.stack = stack;
            this.rows = rows;
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
            return new KeyRingMenu(LockMenuTypes.KEY_RING.get(), id, player, this.stack, this.rows);
        }

        @Override
        public Component getDisplayName() {
            return this.stack.getHoverName();
        }
    }
}
