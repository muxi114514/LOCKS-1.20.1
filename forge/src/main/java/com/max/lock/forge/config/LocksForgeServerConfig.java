package com.max.lock.forge.config;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/**
 * Forge 服务端配置文件（游戏玩法相关）
 * 配置文件路径：config/locks-server.toml
 */
public final class LocksForgeServerConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BREAK_LOCK_RULES;
    public static final ForgeConfigSpec.IntValue STURDY_EXTRA_DAMAGE;
    public static final ForgeConfigSpec.BooleanValue SHOCKING_DAMAGE_ON_BREAK;

    static {
        ForgeConfigSpec.Builder cfg = new ForgeConfigSpec.Builder();

        cfg.comment("Lock Breaking Settings", "暴力破锁相关设置").push("break_lock");

        BREAK_LOCK_RULES = cfg
                .comment("Rules for breaking locks with tools.")
                .comment("Format: \"toolID,durabilityCost,lockID\"")
                .comment("使用工具破锁的规则列表。")
                .comment("格式: \"工具注册名,消耗的耐久值,锁的注册名\"")
                .comment("Example: \"minecraft:iron_axe,125,locks:wood_lock\"")
                .defineList("Break Lock Rules",
                        Lists.newArrayList(
                                "minecraft:iron_axe,125,locks:wood_lock",
                                "minecraft:diamond_axe,780,locks:iron_lock",
                                "minecraft:diamond_axe,1561,locks:diamond_lock",
                                "minecraft:netherite_axe,1015,locks:diamond_lock"),
                        e -> e instanceof String && ((String) e).split(",").length == 3);

        STURDY_EXTRA_DAMAGE = cfg
                .comment("Extra durability cost per level of Sturdy enchantment on the lock.")
                .comment("坚固附魔每一级额外增加的耐久消耗。")
                .defineInRange("Sturdy Extra Damage Per Level", 50, 0, 10000);

        SHOCKING_DAMAGE_ON_BREAK = cfg
                .comment("Whether breaking a lock with Shocking enchantment deals damage to the player.")
                .comment("暴力破锁时，如果锁上有电击附魔，是否对玩家造成伤害。")
                .define("Shocking Damage On Break", true);

        cfg.pop();

        SPEC = cfg.build();
    }

    private LocksForgeServerConfig() {
    }
}
