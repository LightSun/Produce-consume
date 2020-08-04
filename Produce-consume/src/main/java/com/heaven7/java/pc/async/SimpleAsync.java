package com.heaven7.java.pc.async;

import java.util.concurrent.Callable;

/**
 * @since 1.0.5
 * @param <T> the data type
 */
public final class SimpleAsync<T> extends Async<T, T> {

    public SimpleAsync(Callable actTask) {
        super(actTask);
    }

    public SimpleAsync(AsyncManager manager, Callable<T> actTask) {
        super(manager, actTask);
    }
}
