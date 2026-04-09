package com.max.lock.common;

import com.max.lock.common.config.LocksConfig;
import com.max.lock.common.config.LocksServerConfig;
import com.max.lock.common.event.LockEvents;
import com.max.lock.common.init.LockCreativeTab;
import com.max.lock.common.init.LockEnchantments;
import com.max.lock.common.init.LockItems;
import com.max.lock.common.init.LockMenuTypes;
import com.max.lock.common.init.LockRecipeSerializers;
import com.max.lock.common.init.LockSoundEvents;
import com.max.lock.common.network.LockNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;





public class Lock {
    public static final String MOD_ID = "locks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        LockItems.register();
        LockEnchantments.register();
        LockSoundEvents.register();
        LockMenuTypes.register();
        LockRecipeSerializers.register();
        LockCreativeTab.register();
        LockNetwork.register();
        LockEvents.register();
        LocksServerConfig.init();
        LocksConfig.init();
    }
}
