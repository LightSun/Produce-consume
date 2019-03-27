package com.heaven7.java.pc.consumers;

import com.heaven7.java.pc.Consumer;

/**
 * @author heaven7
 */
public abstract class SimpleConsumer<T> implements Consumer<T> {

    @Override
    public void onStart(Runnable next) {
        next.run();
    }
    @Override
    public void onConsume(T obj, Runnable next) {
        next.run();
    }
    @Override
    public void onEnd() {

    }
}
