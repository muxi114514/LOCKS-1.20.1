package com.max.lock.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

/**
 * Accessor：获取 Explosion 的 private level 字段
 */
@Mixin(Explosion.class)
public interface ExplosionAccessor {
    @Accessor("level")
    Level locks$getLevel();
}
