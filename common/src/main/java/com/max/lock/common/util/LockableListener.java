package com.max.lock.common.util;

/**
 * 替代已废弃的 Observable/Observer，监听 Lockable 状态变化
 */
public interface LockableListener {
    /**
     * 当被观察对象状态改变时调用
     * 
     * @param source 发生变化的对象
     */
    void onChanged(Object source);
}
