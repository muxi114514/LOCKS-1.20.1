package com.max.lock.common.item;

import java.util.List;
import java.util.stream.Collectors;

import com.max.lock.common.init.LockSoundEvents;
import com.max.lock.common.menu.KeyRingMenu;
import com.max.lock.common.util.Lockable;
import com.max.lock.common.util.LocksUtil;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * 钥匙圈物品 — 批量携带钥匙并批量匹配锁
 */
public class KeyRingItem extends Item {
    public final int rows;

    public KeyRingItem(int rows, Properties props) {
        super(props.stacksTo(1));
        this.rows = rows;
    }

    /** 检查钥匙圈中是否包含指定 ID 的钥匙 */
    public static boolean containsId(ItemStack stack, int id) {
        if (!stack.hasTag() || !stack.getTag().contains("Items"))
            return false;
        var list = stack.getTag().getList("Items", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int a = 0; a < list.size(); a++) {
            ItemStack keyStack = ItemStack.of(list.getCompound(a));
            if (!keyStack.isEmpty() && LockingItem.getOrSetId(keyStack) == id)
                return true;
        }
        return false;
    }

    /** 右键空气：打开钥匙圈 GUI */
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide && player instanceof ServerPlayer sp) {
            MenuRegistry.openExtendedMenu(sp, new KeyRingMenu.Provider(stack, this.rows), buf -> {
                buf.writeEnum(hand);
                buf.writeInt(this.rows);
            });
        }
        return InteractionResultHolder.pass(stack);
    }

    /** 右键方块：尝试用钥匙圈中的钥匙开锁 */
    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        ItemStack ringStack = ctx.getItemInHand();
        List<Lockable> intersect = LocksUtil.intersecting(world, pos).collect(Collectors.toList());
        if (intersect.isEmpty())
            return InteractionResult.PASS;
        // 遍历钥匙圈中的所有钥匙
        if (!ringStack.hasTag() || !ringStack.getTag().contains("Items"))
            return InteractionResult.PASS;
        var list = ringStack.getTag().getList("Items", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int a = 0; a < list.size(); a++) {
            ItemStack keyStack = ItemStack.of(list.getCompound(a));
            if (keyStack.isEmpty())
                continue;
            int id = LockingItem.getOrSetId(keyStack);
            List<Lockable> match = intersect.stream()
                    .filter(lkb -> lkb.lock.id == id)
                    .collect(Collectors.toList());
            if (match.isEmpty())
                continue;
            world.playSound(ctx.getPlayer(), pos, LockSoundEvents.LOCK_OPEN.get(), SoundSource.BLOCKS, 1f, 1f);
            if (world.isClientSide)
                return InteractionResult.SUCCESS;
            for (Lockable lkb : match)
                lkb.lock.setLocked(!lkb.lock.isLocked());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }
}
