package com.max.lock.common.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.max.lock.common.Lock;
import com.max.lock.common.capability.ILockableHandler;
import com.max.lock.common.capability.ISelection;
import com.max.lock.common.capability.LockCapabilityAccess;
import com.max.lock.common.config.LocksServerConfig;
import com.max.lock.common.init.LockSoundEvents;
import com.max.lock.common.util.Cuboid6i;
import com.max.lock.common.util.LockData;
import com.max.lock.common.util.Lockable;
import com.max.lock.common.util.LocksUtil;
import com.max.lock.common.util.Transform;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;





public class LockItem extends LockingItem {
    public final int length;
    public final int enchantmentValue;
    public final int resistance;

    public LockItem(int length, int enchVal, int resist, Properties props) {
        super(props);
        this.length = length;
        this.enchantmentValue = enchVal;
        this.resistance = resist;
    }



    public static final String KEY_OPEN = "Open";

    public static boolean isOpen(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(KEY_OPEN);
    }

    public static void setOpen(ItemStack stack, boolean open) {
        stack.getOrCreateTag().putBoolean(KEY_OPEN, open);
    }

    public static final String KEY_LENGTH = "Length";

    public static byte getOrSetLength(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (!nbt.contains(KEY_LENGTH))
            nbt.putByte(KEY_LENGTH, (byte) ((LockItem) stack.getItem()).length);
        return nbt.getByte(KEY_LENGTH);
    }

    public static int getResistance(ItemStack stack) {
        return ((LockItem) stack.getItem()).resistance;
    }



    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        if (!LocksServerConfig.canLock(world, pos) || LocksUtil.intersecting(world, pos).findAny().isPresent())
            return InteractionResult.PASS;
        return LocksServerConfig.easyLock ? this.easyLock(ctx) : this.freeLock(ctx);
    }

    
    private InteractionResult easyLock(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        world.playSound(player, pos, LockSoundEvents.LOCK_CLOSE.get(), SoundSource.BLOCKS, 1f, 1f);
        if (world.isClientSide)
            return InteractionResult.SUCCESS;
        BlockState state = world.getBlockState(pos);
        BlockPos pos1 = pos;

        if (state.hasProperty(BlockStateProperties.CHEST_TYPE)
                && state.getValue(BlockStateProperties.CHEST_TYPE) != ChestType.SINGLE)
            pos1 = pos.relative(ChestBlock.getConnectedDirection(state));

        else if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            pos1 = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER
                    ? pos.above()
                    : pos.below();
            if (state.hasProperty(BlockStateProperties.DOOR_HINGE)
                    && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                BlockPos pos2 = pos1.relative(state.getValue(BlockStateProperties.DOOR_HINGE) == DoorHingeSide.LEFT
                        ? dir.getClockWise()
                        : dir.getCounterClockWise());
                if (world.getBlockState(pos2).is(state.getBlock()))
                    pos1 = pos2;
            }
        }
        return this.placeLock(ctx, pos, pos1);
    }

    
    @SuppressWarnings("unused")
    private InteractionResult freeLock(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        BlockPos pos = ctx.getClickedPos();
        ISelection select = LockCapabilityAccess.getSelection(player);
        if (select == null)
            return InteractionResult.PASS;
        BlockPos pos1 = select.get();
        if (pos1 == null) {
            select.set(pos);
            return InteractionResult.SUCCESS;
        }
        Level world = ctx.getLevel();
        select.set(null);
        world.playSound(player, pos, LockSoundEvents.LOCK_CLOSE.get(), SoundSource.BLOCKS, 1f, 1f);
        if (world.isClientSide)
            return InteractionResult.SUCCESS;
        return this.placeLock(ctx, pos1, pos);
    }

    
    private InteractionResult placeLock(UseOnContext ctx, BlockPos pos1, BlockPos pos2) {
        Level world = ctx.getLevel();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        ItemStack lockStack = stack.copy();
        lockStack.setCount(1);
        ILockableHandler handler = LockCapabilityAccess.getHandler(world);
        if (handler == null)
            return InteractionResult.PASS;
        LockData lockData = createLock(stack);
        Transform tr = Transform.fromDirection(ctx.getClickedFace(), player.getDirection().getOpposite());
        Lockable lkb = new Lockable(new Cuboid6i(pos1, pos2), lockData, tr, lockStack, handler.nextId());
        if (!handler.add(lkb))
            return InteractionResult.PASS;
        if (!player.isCreative())
            stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isOpen(stack))
            return super.use(world, player, hand);
        setOpen(stack, false);
        world.playSound(player, player.getX(), player.getY(), player.getZ(),
                LockSoundEvents.PIN_MATCH.get(), SoundSource.PLAYERS, 1f, 1f);
        return super.use(world, player, hand);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, world, lines, flag);
        int len = stack.hasTag() && stack.getTag().contains(KEY_LENGTH)
                ? stack.getTag().getByte(KEY_LENGTH)
                : this.length;
        lines.add(Component.translatable(Lock.MOD_ID + ".tooltip.length",
                ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(len))
                .withStyle(ChatFormatting.DARK_GREEN));
    }

    
    public static LockData createLock(ItemStack stack) {
        return new LockData(LockingItem.getOrSetId(stack), LockItem.getOrSetLength(stack), !LockItem.isOpen(stack));
    }
}
