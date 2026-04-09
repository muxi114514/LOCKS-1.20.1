package com.max.lock.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;




@Mixin(Explosion.class)
public interface ExplosionAccessor {
    @Accessor("level")
    Level locks$getLevel();
}
