package com.heaven7.java.pc.async;

/**
 * @since 1.0.5
 * @param <T> the data type
 */
public interface Action<T> {
    void run(Response<T> result);
}