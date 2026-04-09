package com.max.lock.forge.config;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/**
 * Forge 世界生成锁配置文件
 * 配置文件路径：config/locks-common.toml
 */
public final class LocksForgeWorldGenConfig {
        public static final ForgeConfigSpec SPEC;

        public static final ForgeConfigSpec.DoubleValue GENERATION_CHANCE;
        public static final ForgeConfigSpec.DoubleValue GENERATION_ENCHANT_CHANCE;
        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> GENERATED_LOCKS;
        public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> GENERATED_LOCK_WEIGHTS;
        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> GEN_LOCKABLE_BLOCKS;
        public static final ForgeConfigSpec.BooleanValue RANDOMIZE_LOADED_LOCKS;
        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOOT_TABLE_LOCK_RULES;

        static {
                ForgeConfigSpec.Builder cfg = new ForgeConfigSpec.Builder();

                GENERATION_CHANCE = cfg
                                .comment(
                                                "Chance to generate a random lock on every new chest during world generation. Set to 0 to disable")
                                .comment("在世界生成过程中，每个新容器生成随机锁的概率。设置为0以禁用。")
                                .comment("Tip: Set to 0 and use Loot Table Lock Rules for fine-grained control.")
                                .comment("提示: 设为0并使用战利品表规则可实现精细控制。")
                                .defineInRange("Generation Chance", 0.0d, 0d, 1d);

                GENERATION_ENCHANT_CHANCE = cfg
                                .comment("Chance to randomly enchant a generated lock during world generation. Set to 0 to disable")
                                .comment("在世界生成过程中，随机附魔生成锁的概率。设置为0以禁用。")
                                .defineInRange("Generation Enchant Chance", 0.4d, 0d, 1d);

                GEN_LOCKABLE_BLOCKS = cfg
                                .comment("Blocks that can be locked during world generation (supports regex)")
                                .comment("世界生成时可被加锁的方块列表（支持正则表达式）")
                                .defineList("Lockable Generated Blocks",
                                                Lists.newArrayList("minecraft:chest", "minecraft:barrel",
                                                                "minecraft:trapped_chest"),
                                                e -> e instanceof String);

                GENERATED_LOCKS = cfg
                                .comment("Items that can be generated as locks (must be LockItem instances)")
                                .comment("可作为锁生成的物品列表（必须是 LockItem 类型）")
                                .defineList("Generated Locks",
                                                Lists.newArrayList("locks:wood_lock", "locks:iron_lock",
                                                                "locks:steel_lock",
                                                                "locks:gold_lock", "locks:diamond_lock",
                                                                "locks:netherite_lock"),
                                                e -> e instanceof String);

                GENERATED_LOCK_WEIGHTS = cfg
                                .comment("WARNING: Count must equal Generated Locks count!")
                                .comment("The relative probability for each lock. Higher = more likely")
                                .comment("各锁的相对权重，越大越常见。数量必须与 Generated Locks 一致！")
                                .defineList("Generated Lock Chances",
                                                Lists.newArrayList(5, 4, 2, 3, 2, 1),
                                                e -> e instanceof Integer);

                RANDOMIZE_LOADED_LOCKS = cfg
                                .comment("Randomize lock IDs and combinations when loading from structure files")
                                .comment("从结构文件加载时随机化锁 ID 和密码组合")
                                .define("Randomize Loaded Locks", true);

                LOOT_TABLE_LOCK_RULES = cfg
                                .comment("Lock rules based on loot table. These override the global Generation Chance.")
                                .comment("基于战利品表的锁定规则，优先级高于全局概率。")
                                .comment("Simple format: \"loot_table_path=chance\" (uses global lock list)")
                                .comment("简单格式: \"战利品表路径=概率\"（使用全局锁列表）")
                                .comment("Full format: \"loot_table_path=chance=lock_ids=weights\" (uses custom lock list)")
                                .comment("完整格式: \"战利品表路径=概率=锁ID列表=权重列表\"（使用独立锁列表）")
                                .comment("Supports regex. Example: \"minecraft:chests/village_weaponsmith=1.0\"")
                                .comment("支持正则。示例: \"minecraft:chests/village_.*=0.5=locks:iron_lock,locks:steel_lock=3,2\"")
                                .defineList("Loot Table Lock Rules",
                                                com.google.common.collect.Lists.newArrayList(
                                                                "minecraft:chests/village/village_weaponsmith=1.0=locks:steel_lock=1",
                                                                "minecraft:chests/stronghold_corridor=1.0=locks:gold_lock=1",
                                                                "minecraft:chests/end_city_treasure=1.0=locks:diamond_lock=1"),
                                                e -> e instanceof String && ((String) e).contains("="));

                SPEC = cfg.build();
        }

        private LocksForgeWorldGenConfig() {
        }
}
