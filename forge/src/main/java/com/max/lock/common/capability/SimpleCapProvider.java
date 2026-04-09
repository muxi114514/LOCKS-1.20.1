package com.max.lock.common.capability;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 通用 Capability Provider（Forge 专用）
 * 将接口实例包装为 ICapabilityProvider
 */
public class SimpleCapProvider<T> implements ICapabilityProvider {
    private final Capability<T> cap;
    private final LazyOptional<T> instance;

    public SimpleCapProvider(Capability<T> cap, T instance) {
        this.cap = cap;
        this.instance = LazyOptional.of(() -> instance);
    }

    @NotNull
    @Override
    public <U> LazyOptional<U> getCapability(@NotNull Capability<U> capability, @Nullable Direction side) {
        return this.cap.orEmpty(capability, this.instance);
    }
}
