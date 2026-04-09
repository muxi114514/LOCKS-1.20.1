package com.max.lock.common.item;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.jetbrains.annotations.Nullable;

import com.max.lock.common.Lock;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * 锁和钥匙的基类，管理锁 ID 的生成和显示
 */
public class LockingItem extends Item {
    public LockingItem(Properties props) {
        super(props.stacksTo(1));
    }

    public static final String KEY_ID = "Id";

    /** 将 ID 从一个物品复制到另一个 */
    public static ItemStack copyId(ItemStack from, ItemStack to) {
        to.getOrCreateTag().putInt(KEY_ID, getOrSetId(from));
        return to;
    }

    /** 获取或生成唯一锁 ID */
    public static int getOrSetId(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (!nbt.contains(KEY_ID))
            nbt.putInt(KEY_ID, ThreadLocalRandom.current().nextInt());
        return nbt.getInt(KEY_ID);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (!world.isClientSide)
            getOrSetId(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> lines, TooltipFlag flag) {
        if (stack.hasTag() && stack.getTag().contains(KEY_ID))
            lines.add(Component.translatable(Lock.MOD_ID + ".tooltip.id",
                    ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(getOrSetId(stack)))
                    .withStyle(ChatFormatting.DARK_GREEN));
    }
}
