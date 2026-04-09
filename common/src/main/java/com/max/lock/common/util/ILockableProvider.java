package com.max.lock.common.util;

import java.util.List;

/**
 * ProtoChunk 上的临时锁列表接口
 * 在世界生成阶段存储待注入 Capability 的 Lockable
 */
public interface ILockableProvider {
    List<Lockable> getLockables();
}
