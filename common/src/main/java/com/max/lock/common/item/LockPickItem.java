package com.max.lock.common.item;

import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.max.lock.common.Lock;
import com.max.lock.common.init.LockEnchantments;
import com.max.lock.common.menu.LockPickingMenu;
import com.max.lock.common.util.Lockable;
import com.max.lock.common.util.LocksPredicates;
import com.max.lock.common.util.LocksUtil;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/**
 * 撬锁工具 — 打开撬锁迷你游戏
 */
public class LockPickItem extends Item {
    public static final Component TOO_COMPLEX_MESSAGE = Component.translatable(Lock.MOD_ID + ".status.too_complex");

    public final float strength;
    /** 断裂时回退的销子数量（Integer.MAX_VALUE = 全部归零） */
    public final int resetPins;
    /** 是否免疫通电附魔的魔法伤害 */
    public final boolean shockResistant;

    public LockPickItem(float strength, int resetPins, boolean shockResistant, Properties props) {
        super(props);
        this.strength = strength;
        this.resetPins = resetPins;
        this.shockResistant = shockResistant;
    }

    public static final String KEY_STRENGTH = "Strength";
    public static final String KEY_RESET_PINS = "ResetPins";
    public static final String KEY_SHOCK_RESISTANT = "ShockResistant";

    public static float getOrSetStrength(ItemStack stack) {
        var nbt = stack.getOrCreateTag();
        if (!nbt.contains(KEY_STRENGTH))
            nbt.putFloat(KEY_STRENGTH, ((LockPickItem) stack.getItem()).strength);
        return nbt.getFloat(KEY_STRENGTH);
    }

    public static int getOrSetResetPins(ItemStack stack) {
        var nbt = stack.getOrCreateTag();
        if (!nbt.contains(KEY_RESET_PINS))
            nbt.putInt(KEY_RESET_PINS, ((LockPickItem) stack.getItem()).resetPins);
        return nbt.getInt(KEY_RESET_PINS);
    }

    public static boolean getOrSetShockResistant(ItemStack stack) {
        var nbt = stack.getOrCreateTag();
        if (!nbt.contains(KEY_SHOCK_RESISTANT))
            nbt.putBoolean(KEY_SHOCK_RESISTANT, ((LockPickItem) stack.getItem()).shockResistant);
        return nbt.getBoolean(KEY_SHOCK_RESISTANT);
    }

    public static boolean canPick(ItemStack stack, int complexityLevel) {
        return getOrSetStrength(stack) > complexityLevel * 0.25f;
    }

    public static boolean canPick(ItemStack stack, Lockable lkb) {
        return canPick(stack, EnchantmentHelper.getItemEnchantmentLevel(LockEnchantments.COMPLEXITY.get(), lkb.stack));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        List<Lockable> match = LocksUtil.intersecting(world, pos)
                .filter(LocksPredicates.LOCKED)
                .collect(Collectors.toList());
        if (match.isEmpty())
            return InteractionResult.PASS;
        Lockable lkb = match.get(0);
        if (!canPick(ctx.getItemInHand(), lkb)) {
            if (world.isClientSide)
                ctx.getPlayer().displayClientMessage(TOO_COMPLEX_MESSAGE, true);
            return InteractionResult.PASS;
        }
        if (world.isClientSide)
            return InteractionResult.SUCCESS;
        InteractionHand hand = ctx.getHand();
        if (ctx.getPlayer() instanceof ServerPlayer sp)
            MenuRegistry.openExtendedMenu(sp, new LockPickingMenu.Provider(hand, lkb), buf -> {
                buf.writeEnum(hand);
                buf.writeInt(lkb.id);
            });
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, world, lines, flag);
        float str = stack.hasTag() && stack.getTag().contains(KEY_STRENGTH)
                ? stack.getTag().getFloat(KEY_STRENGTH)
                : this.strength;
        lines.add(Component.translatable(Lock.MOD_ID + ".tooltip.strength",
                ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(str))
                .withStyle(ChatFormatting.DARK_GREEN));
        int rp = stack.hasTag() && stack.getTag().contains(KEY_RESET_PINS)
                ? stack.getTag().getInt(KEY_RESET_PINS)
                : this.resetPins;
        if (rp == 0) {
            lines.add(Component.translatable(Lock.MOD_ID + ".tooltip.reset_pins.none")
                    .withStyle(ChatFormatting.GOLD));
        } else if (rp < Integer.MAX_VALUE) {
            lines.add(Component.translatable(Lock.MOD_ID + ".tooltip.reset_pins",
                    ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(rp))
                    .withStyle(ChatFormatting.YELLOW));
        }

        boolean sr = stack.hasTag() && stack.getTag().contains(KEY_SHOCK_RESISTANT)
                ? stack.getTag().getBoolean(KEY_SHOCK_RESISTANT)
                : this.shockResistant;
        if (sr) {
            lines.add(Component.translatable(Lock.MOD_ID + ".tooltip.shock_resistant")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
}
