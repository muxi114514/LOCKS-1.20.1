package com.max.lock.fabric;

import com.max.lock.common.Lock;
import net.fabricmc.api.ModInitializer;




public class LockModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Lock.init();
    }
}
