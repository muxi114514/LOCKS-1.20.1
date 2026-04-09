package com.max.lock.fabric;

import com.max.lock.common.Lock;
import net.fabricmc.api.ModInitializer;

/**
 * LOCK 模组 Fabric 平台入口点
 */
public class LockModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Lock.init();
    }
}
