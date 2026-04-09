package com.max.lock.common.event;

import com.max.lock.common.capability.ISelection;
import com.max.lock.common.capability.LockCapabilityAccess;
import com.max.lock.common.config.LocksServerConfig;
import com.max.lock.common.init.LockItemTags;
import com.max.lock.common.util.LocksUtil;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;





public final class LockEvents {
    public static final Component LOCKED_MESSAGE = Component.translatable("locks.status.locked");

    private LockEvents() {
    }

    public static void register() {
        TickEvent.PLAYER_PRE.register(LockEvents::onPlayerTick);
    }

    
    private static void onPlayerTick(Player player) {
        ISelection select = LockCapabilityAccess.getSelection(player);
        if (select == null || select.get() == null)
            return;
        for (ItemStack stack : player.getHandSlots())
            if (stack.is(LockItemTags.LOCKS))
                return;
        select.set(null);
    }

    
    public static boolean canBreakLockable(Player player, BlockPos pos) {
        return !LocksServerConfig.protectLockables || player.isCreative() || !LocksUtil.locked(player.level(), pos);
    }
}
