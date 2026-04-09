package com.max.lock;

import com.google.common.collect.Lists;
import com.max.lock.client.event.LocksClientEvents;
import com.max.lock.client.init.LockScreens;
import com.max.lock.client.util.LocksClientUtil;
import com.max.lock.common.capability.forge.LockCapabilityAccessImpl;
import com.max.lock.common.event.LockEvents;
import com.max.lock.common.util.Lockable;
import com.max.lock.common.util.LocksUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.joml.Vector3f;

// Forge platform events
public final class LockForgeEvents {
    private LockForgeEvents() {
    }

    @Mod.EventBusSubscriber(modid = LockMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent e) {
            e.enqueueWork(() -> {
                LockScreens.register();
                com.max.lock.client.init.LockItemModelsProperties.register();
            });
        }

        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent e) {
            LockCapabilityAccessImpl.registerCapabilities(e);
        }

        @SubscribeEvent
        public static void onConfigLoad(net.minecraftforge.fml.event.config.ModConfigEvent e) {
            if (e.getConfig().getSpec() == com.max.lock.forge.config.LocksForgeWorldGenConfig.SPEC) {
                com.max.lock.common.config.LocksConfig.generationChance = com.max.lock.forge.config.LocksForgeWorldGenConfig.GENERATION_CHANCE
                        .get();
                com.max.lock.common.config.LocksConfig.generationEnchantChance = com.max.lock.forge.config.LocksForgeWorldGenConfig.GENERATION_ENCHANT_CHANCE
                        .get();
                com.max.lock.common.config.LocksConfig.lockableGenBlocks = new java.util.ArrayList<>(
                        com.max.lock.forge.config.LocksForgeWorldGenConfig.GEN_LOCKABLE_BLOCKS.get());
                com.max.lock.common.config.LocksConfig.generatedLocks = new java.util.ArrayList<>(
                        com.max.lock.forge.config.LocksForgeWorldGenConfig.GENERATED_LOCKS.get());
                com.max.lock.common.config.LocksConfig.generatedLockWeights = new java.util.ArrayList<>(
                        com.max.lock.forge.config.LocksForgeWorldGenConfig.GENERATED_LOCK_WEIGHTS.get());
                com.max.lock.common.config.LocksConfig.randomizeLoadedLocks = com.max.lock.forge.config.LocksForgeWorldGenConfig.RANDOMIZE_LOADED_LOCKS
                        .get();
                com.max.lock.common.config.LocksConfig.lootTableLockRules = new java.util.ArrayList<>(
                        com.max.lock.forge.config.LocksForgeWorldGenConfig.LOOT_TABLE_LOCK_RULES.get());
                com.max.lock.common.config.LocksConfig.init();
            }
            if (e.getConfig().getSpec() == com.max.lock.forge.config.LocksForgeServerConfig.SPEC) {
                com.max.lock.common.config.LocksServerConfig.breakLockRules = new java.util.ArrayList<>(
                        com.max.lock.forge.config.LocksForgeServerConfig.BREAK_LOCK_RULES.get());
                com.max.lock.common.config.LocksServerConfig.sturdyExtraDamage = com.max.lock.forge.config.LocksForgeServerConfig.STURDY_EXTRA_DAMAGE
                        .get();
                com.max.lock.common.config.LocksServerConfig.shockingDamageOnBreak = com.max.lock.forge.config.LocksForgeServerConfig.SHOCKING_DAMAGE_ON_BREAK
                        .get();
                com.max.lock.common.config.LocksServerConfig.reloadBreakRules();
            }
        }
    }

    @Mod.EventBusSubscriber(modid = LockMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ForgeEvents {
        @SubscribeEvent
        public static void onBlockBreaking(PlayerEvent.BreakSpeed e) {
            if (e.getPosition().isPresent() && !LockEvents.canBreakLockable(e.getEntity(), e.getPosition().get()))
                e.setCanceled(true);
        }

        @SubscribeEvent
        public static void onBlockBreak(BlockEvent.BreakEvent e) {
            if (!LockEvents.canBreakLockable(e.getPlayer(), e.getPos()))
                e.setCanceled(true);
        }

        // 锁核心交互：使用 Forge 原生事件正确拦截方块交互
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
        public static void onRightClick(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock e) {
            var pos = e.getPos();
            var world = e.getLevel();
            var player = e.getEntity();
            var handler = com.max.lock.common.capability.LockCapabilityAccess.getHandler(world);
            if (handler == null)
                return;
            var intersect = handler.getInChunk(pos).values().stream()
                    .filter(lkb -> lkb.bb.intersects(pos))
                    .toArray(Lockable[]::new);
            if (intersect.length == 0)
                return;

            if (e.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) {
                e.setUseBlock(net.minecraftforge.eventbus.api.Event.Result.DENY);
                return;
            }
            var stack = e.getItemStack();
            var locked = java.util.Arrays.stream(intersect)
                    .filter(com.max.lock.common.util.LocksPredicates.LOCKED).findFirst();

            if (locked.isPresent()) {
                Lockable lkb = locked.get();
                // 阻止方块交互（如打开箱子），但允许物品交互（如万能钥匙的 useOn）
                e.setUseBlock(net.minecraftforge.eventbus.api.Event.Result.DENY);
                var item = stack.getItem();
                boolean isValidUnlocker = stack.is(com.max.lock.common.init.LockItemTags.LOCK_PICKS)
                        || item == com.max.lock.common.init.LockItems.MASTER_KEY.get()
                        || (stack.is(com.max.lock.common.init.LockItemTags.KEYS)
                                && com.max.lock.common.item.LockingItem.getOrSetId(stack) == lkb.lock.id)
                        || (item instanceof com.max.lock.common.item.KeyRingItem
                                && com.max.lock.common.item.KeyRingItem.containsId(stack, lkb.lock.id));
                if (!isValidUnlocker) {
                    // 暴力破锁判定
                    var breakRule = findBreakRule(stack, lkb);
                    if (breakRule != null) {
                        int sturdyLevel = net.minecraft.world.item.enchantment.EnchantmentHelper
                                .getItemEnchantmentLevel(
                                        com.max.lock.common.init.LockEnchantments.STURDY.get(), lkb.stack);
                        int finalCost = breakRule.damageCost
                                + sturdyLevel * com.max.lock.common.config.LocksServerConfig.sturdyExtraDamage;
                        int remainingDurability = stack.getMaxDamage() - stack.getDamageValue();

                        if (remainingDurability < finalCost) {
                            // 耐久不足，提示玩家
                            player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                            if (world.isClientSide)
                                player.displayClientMessage(
                                        net.minecraft.network.chat.Component
                                                .translatable("locks.status.durability_insufficient"),
                                        true);
                            world.playSound(player, pos,
                                    net.minecraft.sounds.SoundEvents.ANVIL_LAND,
                                    net.minecraft.sounds.SoundSource.BLOCKS, 0.3f, 1.5f);
                            return;
                        }

                        // 执行破锁（仅服务端）
                        if (!world.isClientSide) {
                            // 扣除耐久（通过 hurtAndBreak 兼容原版耐久附魔）
                            stack.hurtAndBreak(finalCost, player,
                                    p -> p.broadcastBreakEvent(e.getHand()));
                            // 掉落锁物品
                            world.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                                    world, pos.getX() + 0.5d, pos.getY() + 0.5d,
                                    pos.getZ() + 0.5d, lkb.stack.copy()));
                            // 电击附魔惩罚
                            if (com.max.lock.common.config.LocksServerConfig.shockingDamageOnBreak) {
                                int shockLevel = net.minecraft.world.item.enchantment.EnchantmentHelper
                                        .getItemEnchantmentLevel(
                                                com.max.lock.common.init.LockEnchantments.SHOCKING.get(),
                                                lkb.stack);
                                if (shockLevel > 0) {
                                    player.hurt(player.damageSources().magic(), shockLevel * 1.5f);
                                    world.playSound(null, player.getX(), player.getY(),
                                            player.getZ(),
                                            com.max.lock.common.init.LockSoundEvents.SHOCK.get(),
                                            net.minecraft.sounds.SoundSource.BLOCKS, 1f, 1f);
                                }
                            }
                            // 移除锁实体
                            handler.remove(lkb.id);
                        }
                        // 播放破锁音效
                        world.playSound(player, pos,
                                net.minecraft.sounds.SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR,
                                net.minecraft.sounds.SoundSource.BLOCKS, 0.6f, 1.2f);
                        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                        return;
                    }
                    // 无匹配规则：播放锁摇晃
                    lkb.swing(20);
                    world.playSound(player, pos, com.max.lock.common.init.LockSoundEvents.LOCK_RATTLE.get(),
                            net.minecraft.sounds.SoundSource.BLOCKS, 1f, 1f);
                }
                player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                if (world.isClientSide)
                    player.displayClientMessage(LockEvents.LOCKED_MESSAGE, true);
                return;
            }

            // 空手蹲下移除已开锁
            if (com.max.lock.common.config.LocksServerConfig.allowRemovingLocks
                    && player.isShiftKeyDown() && stack.isEmpty()) {
                var match = java.util.Arrays.stream(intersect)
                        .filter(com.max.lock.common.util.LocksPredicates.NOT_LOCKED)
                        .toArray(Lockable[]::new);
                if (match.length == 0)
                    return;
                e.setUseBlock(net.minecraftforge.eventbus.api.Event.Result.DENY);
                world.playSound(player, pos, net.minecraft.sounds.SoundEvents.IRON_DOOR_OPEN,
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 0.8f + world.random.nextFloat() * 0.4f);
                player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                if (!world.isClientSide)
                    for (Lockable lkb : match) {
                        world.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                                world, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, lkb.stack));
                        handler.remove(lkb.id);
                    }
            }
        }

        /** 查找匹配的破锁规则 */
        private static com.max.lock.common.config.LocksServerConfig.BreakRule findBreakRule(
                net.minecraft.world.item.ItemStack toolStack, Lockable lockable) {
            var rules = com.max.lock.common.config.LocksServerConfig.getBreakRules();
            var toolItem = toolStack.getItem();
            var lockItem = lockable.stack.getItem();
            for (var rule : rules) {
                if (rule.tool == toolItem && rule.targetLock == lockItem)
                    return rule;
            }
            return null;
        }

        // 红石信号屏蔽：锁住的方块不传播邻居更新
        @SubscribeEvent
        public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent e) {
            if (e.getLevel() instanceof Level level && LocksUtil.locked(level, e.getPos()))
                e.setCanceled(true);
        }

        // ===== Capability 附加 =====

        @SubscribeEvent
        public static void onAttachWorldCaps(AttachCapabilitiesEvent<Level> e) {
            LockCapabilityAccessImpl.attachToWorld(e);
        }

        @SubscribeEvent
        public static void onAttachChunkCaps(AttachCapabilitiesEvent<LevelChunk> e) {
            LockCapabilityAccessImpl.attachToChunk(e);
        }

        @SubscribeEvent
        public static void onAttachEntityCaps(AttachCapabilitiesEvent<Entity> e) {
            LockCapabilityAccessImpl.attachToEntity(e);
        }
    }

    @Mod.EventBusSubscriber(modid = LockMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ClientEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent e) {
            if (e.phase != TickEvent.Phase.START)
                return;
            LocksClientEvents.onClientTick(Minecraft.getInstance());
        }

        @SubscribeEvent
        public static void onRenderLevel(RenderLevelStageEvent e) {
            if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
                return;
            Minecraft mc = Minecraft.getInstance();
            PoseStack mtx = e.getPoseStack();
            MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
            LocksClientEvents.renderLocks(mtx, buf, e.getPartialTick());
            LocksClientEvents.renderSelection(mtx, buf);
        }

        @SubscribeEvent
        public static void onRenderOverlay(RenderGuiOverlayEvent.Pre e) {
            Minecraft mc = Minecraft.getInstance();
            Lockable lkb = LocksClientEvents.tooltipLockable;
            if (lkb == null)
                return;
            if (LocksClientEvents.holdingPick(mc.player)) {
                PoseStack mtx = e.getGuiGraphics().pose();
                Vector3f vec = LocksClientUtil.worldToScreen(lkb.getLockState(mc.level).pos, e.getPartialTick());
                if (vec.z() < 0d) {
                    mtx.pushPose();
                    mtx.translate(vec.x(), vec.y(), 0f);
                    LocksClientEvents.renderHudTooltip(mtx,
                            Lists.transform(
                                    lkb.stack.getTooltipLines(mc.player,
                                            mc.options.advancedItemTooltips ? TooltipFlag.ADVANCED
                                                    : TooltipFlag.NORMAL),
                                    net.minecraft.network.chat.Component::getVisualOrderText),
                            mc.font);
                    mtx.popPose();
                }
            }
            LocksClientEvents.tooltipLockable = null;
        }
    }
}
