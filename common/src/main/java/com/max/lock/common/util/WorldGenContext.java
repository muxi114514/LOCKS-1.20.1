package com.max.lock.common.util;

import net.minecraft.server.level.WorldGenRegion;







public final class WorldGenContext {
    private WorldGenContext() {
    }

    



    public static final ThreadLocal<WorldGenRegion> CURRENT_REGION = new ThreadLocal<>();
}
