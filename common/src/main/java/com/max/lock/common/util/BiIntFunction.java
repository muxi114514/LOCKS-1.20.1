package com.max.lock.common.util;

/**
 * 接受两个 int 参数并返回结果的函数式接口
 */
@FunctionalInterface
public interface BiIntFunction<T> {
    T apply(int x, int y);
}
