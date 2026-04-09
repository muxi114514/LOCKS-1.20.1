package com.max.lock;

import com.max.lock.common.Lock;
import com.max.lock.forge.config.LocksForgeServerConfig;
import com.max.lock.forge.config.LocksForgeWorldGenConfig;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * LOCK 模组 Forge 平台入口点
 */
@Mod(LockMod.MOD_ID)
public class LockMod {
    public static final String MOD_ID = "locks";

    public LockMod() {
        EventBuses.registerModEventBus(MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, LocksForgeWorldGenConfig.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, LocksForgeServerConfig.SPEC);
        Lock.init();
    }
}
