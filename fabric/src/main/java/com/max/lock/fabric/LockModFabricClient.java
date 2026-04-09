package com.max.lock.fabric;

import com.max.lock.client.init.LockScreens;
import net.fabricmc.api.ClientModInitializer;

// Fabric client initialization
public class LockModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LockScreens.register();
    }
}
