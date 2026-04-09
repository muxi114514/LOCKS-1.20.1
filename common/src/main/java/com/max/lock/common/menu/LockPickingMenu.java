package com.max.lock.common.menu;

import com.max.lock.common.Lock;
import com.max.lock.common.capability.ILockableHandler;
import com.max.lock.common.capability.LockCapabilityAccess;
import com.max.lock.common.init.LockEnchantments;
import com.max.lock.common.init.LockItemTags;
import com.max.lock.common.init.LockMenuTypes;
import com.max.lock.common.init.LockSoundEvents;
import com.max.lock.common.item.LockPickItem;
import com.max.lock.common.network.TryPinResultPacket;
import com.max.lock.common.util.Lockable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

/**
 * 撬锁迷你游戏菜单
 * 服务端处理 pin 判定，通过网络包同步结果到客户端
 */
public class LockPickingMenu extends AbstractContainerMenu {
    public static final Component TITLE = Component.translatable(Lock.MOD_ID + ".gui.lockpicking.title");

    public final Player player;
    public final InteractionHand hand;
    public final Lockable lockable;
    public final Vec3 pos;
    public final int shocking, sturdy, complexity;
    protected int currIndex = 0;

    public LockPickingMenu(MenuType<?> type, int id, Player player, InteractionHand hand, Lockable lkb) {
        super(type, id);
        this.player = player;
        this.hand = hand;
        this.lockable = lkb;
        Lockable.State state = lkb.getLockState(player.level());
        this.pos = state == null ? lkb.bb.center() : state.pos;
        this.shocking = EnchantmentHelper.getItemEnchantmentLevel(LockEnchantments.SHOCKING.get(), lkb.stack);
        this.sturdy = EnchantmentHelper.getItemEnchantmentLevel(LockEnchantments.STURDY.get(), lkb.stack);
        this.complexity = EnchantmentHelper.getItemEnchantmentLevel(LockEnchantments.COMPLEXITY.get(), lkb.stack);

        // 隐藏的玩家背包同步
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new HiddenSlot(player.getInventory(), col + row * 9 + 9, 0, 0));
        for (int col = 0; col < 9; ++col)
            this.addSlot(new HiddenSlot(player.getInventory(), col, 0, 0));
    }

    /** 隐藏插槽 — 保持背包同步但不在 GUI 中显示 */
    private static class HiddenSlot extends Slot {
        public HiddenSlot(net.minecraft.world.Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean isActive() {
            return false;
        }
    }

    public boolean isValidPick(ItemStack stack) {
        return stack.is(LockItemTags.LOCK_PICKS) && LockPickItem.canPick(stack, this.complexity);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.lockable.lock.isLocked() && this.isValidPick(player.getItemInHand(this.hand));
    }

    public boolean isOpen() {
        return this.currIndex == this.lockable.lock.getLength();
    }

    /** 回退指定步数的开锁进度 */
    protected void reset(int steps) {
        this.currIndex = Math.max(0, this.currIndex - steps);
    }

    /** 服务端处理撬锁尝试 */
    public void tryPin(int pin) {
        if (this.isOpen())
            return;
        boolean correct = false;
        int resetCount = 0;
        if (this.lockable.lock.checkPin(this.currIndex, pin)) {
            ++this.currIndex;
            correct = true;
            this.player.level().playSound(null, this.pos.x, this.pos.y, this.pos.z,
                    LockSoundEvents.PIN_MATCH.get(), SoundSource.BLOCKS, 1f, 1f);
        } else {
            // 在断裂判定前缓存当前撬锁器的属性（tryBreakPick 会销毁原物品）
            ItemStack pickBeforeBreak = this.player.getItemInHand(this.hand);
            int resetPins = pickBeforeBreak.getItem() instanceof LockPickItem
                    ? LockPickItem.getOrSetResetPins(pickBeforeBreak)
                    : Integer.MAX_VALUE;
            boolean shockResistant = pickBeforeBreak.getItem() instanceof LockPickItem
                    && LockPickItem.getOrSetShockResistant(pickBeforeBreak);
            if (this.tryBreakPick(this.player, pin)) {
                resetCount = Math.min(resetPins, this.currIndex);
                this.reset(resetCount);
                // 电击附魔效果（如果工具不免疫魔法）
                if (this.shocking > 0 && !shockResistant) {
                    this.player.hurt(this.player.damageSources().magic(), this.shocking * 1.5f);
                    this.player.level().playSound(null, this.player.getX(), this.player.getY(), this.player.getZ(),
                            LockSoundEvents.SHOCK.get(), SoundSource.BLOCKS, 1f, 1f);
                }
            } else {
                this.player.level().playSound(null, this.pos.x, this.pos.y, this.pos.z,
                        LockSoundEvents.PIN_FAIL.get(), SoundSource.BLOCKS, 1f, 1f);
            }
        }
        if (this.player instanceof ServerPlayer sp)
            TryPinResultPacket.send(sp, correct, resetCount);
    }

    /** 客户端处理撬锁结果 */
    public void handlePin(boolean correct, int resetCount) {
        if (correct)
            ++this.currIndex;
        if (resetCount > 0)
            this.reset(resetCount);
    }

    /** 尝试破坏撬锁工具 */
    protected boolean tryBreakPick(Player player, int pin) {
        ItemStack pickStack = player.getItemInHand(this.hand);
        float sturdyMod = this.sturdy == 0 ? 1f : 0.75f + this.sturdy * 0.5f;
        float ch = LockPickItem.getOrSetStrength(pickStack) / sturdyMod;
        float ex = (1f - ch) * (1f - this.getBreakChanceMultiplier(pin));
        if (!pickStack.is(LockItemTags.LOCK_PICKS) || player.level().random.nextFloat() < ex + ch)
            return false;
        player.broadcastBreakEvent(this.hand);
        pickStack.shrink(1);
        // 自动替换手中的撬锁工具
        if (pickStack.isEmpty()) {
            for (int a = 0; a < player.getInventory().getContainerSize(); ++a) {
                ItemStack stack = player.getInventory().getItem(a);
                if (this.isValidPick(stack)) {
                    player.setItemInHand(this.hand, stack);
                    player.getInventory().removeItemNoUpdate(a);
                    break;
                }
            }
        }
        return true;
    }

    protected float getBreakChanceMultiplier(int pin) {
        return Math.abs(this.lockable.lock.getPin(this.currIndex) - pin) == 1 ? 0.33f : 1f;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!this.isOpen() || !this.lockable.lock.isLocked())
            return;
        this.lockable.lock.setLocked(false);
        this.player.level().playSound(player, this.pos.x, this.pos.y, this.pos.z,
                LockSoundEvents.LOCK_OPEN.get(), SoundSource.BLOCKS, 1f, 1f);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    // ===== 工厂和 Provider =====

    /** 从网络 buf 创建客户端菜单 */
    public static LockPickingMenu fromNetwork(int id, Inventory inv, FriendlyByteBuf buf) {
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        int lockableId = buf.readInt();
        ILockableHandler handler = LockCapabilityAccess.getHandler(inv.player.level());
        Lockable lkb = handler != null ? handler.getLoaded().get(lockableId) : null;
        return new LockPickingMenu(LockMenuTypes.LOCK_PICKING.get(), id, inv.player, hand, lkb);
    }

    /** 服务端 MenuProvider */
    public static class Provider implements MenuProvider {
        public final InteractionHand hand;
        public final Lockable lockable;

        public Provider(InteractionHand hand, Lockable lkb) {
            this.hand = hand;
            this.lockable = lkb;
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
            return new LockPickingMenu(LockMenuTypes.LOCK_PICKING.get(), id, player, this.hand, this.lockable);
        }

        @Override
        public Component getDisplayName() {
            return TITLE;
        }
    }
}
