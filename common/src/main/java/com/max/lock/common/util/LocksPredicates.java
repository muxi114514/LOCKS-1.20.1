package com.max.lock.common.util;

import java.util.function.Predicate;

/**
 * Lockable 常用谓词
 */
public final class LocksPredicates {
    public static final Predicate<Lockable> LOCKED = lkb -> lkb.lock.isLocked();
    public static final Predicate<Lockable> NOT_LOCKED = lkb -> !lkb.lock.isLocked();

    private LocksPredicates() {
    }
}
