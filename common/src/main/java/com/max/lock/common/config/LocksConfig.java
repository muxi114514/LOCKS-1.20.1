package com.max.lock.common.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import com.max.lock.common.Lock;
import com.max.lock.common.util.LocksUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;

/**
 * 跨平台世界生成配置
 * Forge 平台通过 LocksForgeWorldGenConfig 覆盖默认值
 */
public final class LocksConfig {
    // 世界生成锁概率（默认关闭，由战利品表规则精细控制）
    public static double generationChance = 0.0d;
    // 附魔概率
    public static double generationEnchantChance = 0.4d;
    // 可锁方块白名单（正则表达式）
    public static List<String> lockableGenBlocks = Arrays.asList(
            "minecraft:chest", "minecraft:barrel", "minecraft:trapped_chest");
    // 锁类型
    public static List<String> generatedLocks = Arrays.asList(
            "locks:wood_lock", "locks:iron_lock", "locks:steel_lock", "locks:gold_lock", "locks:diamond_lock",
            "locks:netherite_lock");
    // 权重
    public static List<Integer> generatedLockWeights = Arrays.asList(5, 4, 2, 3, 2, 1);
    // 结构加载时随机化
    public static boolean randomizeLoadedLocks = true;

    // ===== 战利品表锁定规则 =====
    // 格式: "战利品表路径=概率" 或 "战利品表路径=概率=锁列表=权重列表"
    // 注意：矿车箱子（如矿道）是实体，不支持加锁，仅支持方块容器
    public static List<String> lootTableLockRules = Arrays.asList(
            "minecraft:chests/village/village_weaponsmith=1.0=locks:steel_lock=1",
            "minecraft:chests/stronghold_corridor=1.0=locks:gold_lock=1",
            "minecraft:chests/end_city_treasure=1.0=locks:diamond_lock=1"
    );

    private static NavigableMap<Integer, Item> weightedLocks;
    private static int weightTotal;
    private static Pattern[] compiledGenPatterns;
    // 解析后的战利品表规则（线程安全）
    private static volatile List<LootTableRule> parsedLootTableRules;

    private LocksConfig() {
    }

    /**
     * 战利品表锁定规则数据类
     * 将字符串配置解析为类型化对象
     */
    public static final class LootTableRule {
        public final Pattern pattern;
        public final double chance;
        // 独立锁列表（null 表示使用全局列表）
        public final NavigableMap<Integer, Item> weightedLocks;
        public final int weightTotal;

        public LootTableRule(Pattern pattern, double chance,
                NavigableMap<Integer, Item> weightedLocks, int weightTotal) {
            this.pattern = pattern;
            this.chance = chance;
            this.weightedLocks = weightedLocks;
            this.weightTotal = weightTotal;
        }

        /** 是否使用独立锁列表 */
        public boolean hasCustomLocks() {
            return weightedLocks != null && weightTotal > 0;
        }
    }

    /** 初始化权重表和正则 */
    public static void init() {
        weightedLocks = new TreeMap<>();
        weightTotal = 0;
        for (int a = 0; a < generatedLocks.size(); a++) {
            weightTotal += generatedLockWeights.get(a);
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(generatedLocks.get(a)));
            weightedLocks.put(weightTotal, item);
        }
        compiledGenPatterns = lockableGenBlocks.stream()
                .map(Pattern::compile)
                .toArray(Pattern[]::new);
        reloadLootTableRules();
    }

    /** 解析战利品表锁定规则 */
    public static void reloadLootTableRules() {
        List<LootTableRule> rules = new CopyOnWriteArrayList<>();
        for (String entry : lootTableLockRules) {
            try {
                String[] parts = entry.split("=");
                if (parts.length < 2) {
                    Lock.LOGGER.warn("无效的战利品表规则格式: {}", entry);
                    continue;
                }
                Pattern pattern = Pattern.compile(parts[0].trim());
                double chance = Double.parseDouble(parts[1].trim());

                NavigableMap<Integer, Item> customLocks = null;
                int customTotal = 0;

                // 完整格式: 路径=概率=锁列表=权重列表
                if (parts.length >= 4) {
                    String[] lockIds = parts[2].trim().split(",");
                    String[] weightStrs = parts[3].trim().split(",");
                    if (lockIds.length != weightStrs.length) {
                        Lock.LOGGER.warn("锁列表与权重数量不匹配: {}", entry);
                        continue;
                    }
                    customLocks = new TreeMap<>();
                    for (int i = 0; i < lockIds.length; i++) {
                        int w = Integer.parseInt(weightStrs[i].trim());
                        customTotal += w;
                        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(lockIds[i].trim()));
                        customLocks.put(customTotal, item);
                    }
                }
                rules.add(new LootTableRule(pattern, chance, customLocks, customTotal));
            } catch (Exception e) {
                Lock.LOGGER.warn("解析战利品表规则失败: {} - {}", entry, e.getMessage());
            }
        }
        parsedLootTableRules = rules;
    }

    /** 检查方块是否在世界生成白名单中且概率通过 */
    public static boolean canGen(RandomSource rng, Block block) {
        if (compiledGenPatterns == null)
            init();
        if (!LocksUtil.chance(rng, generationChance))
            return false;
        String name = BuiltInRegistries.BLOCK.getKey(block).toString();
        for (Pattern p : compiledGenPatterns)
            if (p.matcher(name).matches())
                return true;
        return false;
    }

    /**
     * 根据战利品表查找匹配的锁定规则
     * 
     * @return 匹配的规则，无匹配返回 null
     */
    public static LootTableRule findLootTableRule(ResourceLocation lootTable) {
        if (parsedLootTableRules == null)
            reloadLootTableRules();
        String path = lootTable.toString();
        for (LootTableRule rule : parsedLootTableRules) {
            if (rule.pattern.matcher(path).matches())
                return rule;
        }
        return null;
    }

    /**
     * 检查战利品表规则是否允许加锁
     * 
     * @return true=应加锁，false=不应加锁，null=无匹配规则
     */
    public static Boolean canGenByLootTable(RandomSource rng, ResourceLocation lootTable) {
        LootTableRule rule = findLootTableRule(lootTable);
        if (rule == null)
            return null;
        return LocksUtil.chance(rng, rule.chance);
    }

    public static boolean canEnchant(RandomSource rng) {
        return LocksUtil.chance(rng, generationEnchantChance);
    }

    /** 按权重随机选择一种锁并可能附魔 */
    public static ItemStack getRandomLock(RandomSource rng) {
        if (weightedLocks == null || weightTotal <= 0)
            init();
        ItemStack stack = new ItemStack(weightedLocks.ceilingEntry(rng.nextInt(weightTotal) + 1).getValue());
        return canEnchant(rng) ? EnchantmentHelper.enchantItem(rng, stack, 5 + rng.nextInt(30), false) : stack;
    }

    /** 根据战利品表规则获取锁（优先使用规则独立锁列表） */
    public static ItemStack getRandomLockForRule(RandomSource rng, LootTableRule rule) {
        if (rule.hasCustomLocks()) {
            ItemStack stack = new ItemStack(
                    rule.weightedLocks.ceilingEntry(rng.nextInt(rule.weightTotal) + 1).getValue());
            return canEnchant(rng) ? EnchantmentHelper.enchantItem(rng, stack, 5 + rng.nextInt(30), false) : stack;
        }
        return getRandomLock(rng);
    }
}
