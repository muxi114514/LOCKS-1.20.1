package com.max.lock.common.config;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * 跨平台服务端配置
 * 使用静态字段存储默认值，各平台可通过配置文件覆盖
 */
public final class LocksServerConfig {
    // 可上锁的最大方块体积
    public static int maxLockableVolume = 6;
    // 可上锁方块正则匹配列表
    public static List<String> lockableBlockPatterns = Arrays.asList(
            ".*chest", ".*barrel", ".*hopper", ".*door", ".*trapdoor", ".*fence_gate", ".*shulker_box");
    // 允许空手蹲下移除已开锁的锁
    public static boolean allowRemovingLocks = true;
    // 被锁方块不可在生存模式中破坏
    public static boolean protectLockables = true;
    // 一键锁定模式
    public static boolean easyLock = true;

    // ===== 暴力破锁配置 =====
    // 格式: "工具ID,耐久消耗数值,锁ID"
    public static List<String> breakLockRules = Arrays.asList(
            "minecraft:iron_axe,125,locks:wood_lock",
            "minecraft:diamond_axe,780,locks:iron_lock",
            "minecraft:diamond_axe,1561,locks:diamond_lock",
            "minecraft:netherite_axe,1015,locks:diamond_lock");
    // 坚固附魔每级额外消耗的耐久值
    public static int sturdyExtraDamage = 50;
    // 破锁时是否触发电击附魔伤害
    public static boolean shockingDamageOnBreak = true;

    private static Pattern[] compiledPatterns;
    // 解析后的破锁规则缓存（线程安全）
    private static volatile List<BreakRule> parsedBreakRules;

    private LocksServerConfig() {
    }

    /**
     * 破锁规则数据类
     * 将字符串配置解析为运行时可用的类型化对象
     */
    public static final class BreakRule {
        public final Item tool;
        public final int damageCost;
        public final Item targetLock;

        public BreakRule(Item tool, int damageCost, Item targetLock) {
            this.tool = tool;
            this.damageCost = damageCost;
            this.targetLock = targetLock;
        }
    }

    /** 编译正则表达式并解析破锁规则 */
    public static void init() {
        compiledPatterns = lockableBlockPatterns.stream()
                .map(Pattern::compile)
                .toArray(Pattern[]::new);
        reloadBreakRules();
    }

    /** 重新解析破锁规则列表 */
    public static void reloadBreakRules() {
        List<BreakRule> rules = new CopyOnWriteArrayList<>();
        for (String entry : breakLockRules) {
            String[] parts = entry.split(",");
            if (parts.length != 3) {
                com.max.lock.common.Lock.LOGGER.warn("无效的破锁规则格式: {}", entry);
                continue;
            }
            Item tool = BuiltInRegistries.ITEM.get(new ResourceLocation(parts[0].trim()));
            int cost;
            try {
                cost = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                com.max.lock.common.Lock.LOGGER.warn("无效的耐久数值: {}", entry);
                continue;
            }
            Item lock = BuiltInRegistries.ITEM.get(new ResourceLocation(parts[2].trim()));
            if (tool == Items.AIR || lock == Items.AIR) {
                com.max.lock.common.Lock.LOGGER.warn("未找到对应物品: {}", entry);
                continue;
            }
            rules.add(new BreakRule(tool, cost, lock));
        }
        parsedBreakRules = rules;
    }

    /** 获取解析后的破锁规则（只读） */
    public static List<BreakRule> getBreakRules() {
        if (parsedBreakRules == null)
            reloadBreakRules();
        return parsedBreakRules;
    }

    /** 检查指定方块是否可以上锁 */
    public static boolean canLock(Level world, BlockPos pos) {
        if (compiledPatterns == null)
            init();
        String name = BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos).getBlock()).toString();
        for (Pattern p : compiledPatterns)
            if (p.matcher(name).matches())
                return true;
        return false;
    }
}
