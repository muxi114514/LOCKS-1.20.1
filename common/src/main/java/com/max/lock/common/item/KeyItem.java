package com.max.lock.common.item;

import java.util.List;
import java.util.stream.Collectors;

import com.max.lock.common.init.LockSoundEvents;
import com.max.lock.common.util.Lockable;
import com.max.lock.common.util.LocksUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * 钥匙物品 — 用于开锁/锁定与自身 ID 匹配的锁
 */
public class KeyItem extends LockingItem {
    public KeyItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        int id = getOrSetId(ctx.getItemInHand());
        List<Lockable> match = LocksUtil.intersecting(world, pos)
                .filter(lkb -> lkb.lock.id == id)
                .collect(Collectors.toList());
        if (match.isEmpty())
            return InteractionResult.PASS;
        world.playSound(ctx.getPlayer(), pos, LockSoundEvents.LOCK_OPEN.get(), SoundSource.BLOCKS, 1f, 1f);
        if (world.isClientSide)
            return InteractionResult.SUCCESS;
        for (Lockable lkb : match)
            lkb.lock.setLocked(!lkb.lock.isLocked());
        return InteractionResult.SUCCESS;
    }
}
