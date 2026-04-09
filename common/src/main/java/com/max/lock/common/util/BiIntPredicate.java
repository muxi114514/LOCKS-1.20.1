package com.max.lock.common.util;

/**
 * 接受两个 int 参数并返回 boolean 的函数式接口
 */
@FunctionalInterface
public interface BiIntPredicate {
    boolean test(int x, int y);
}
