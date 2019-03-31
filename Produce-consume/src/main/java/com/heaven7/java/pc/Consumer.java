package com.heaven7.java.pc;

/**
 * the consumer which can consume the product by {@linkplain #onConsume(Object, Runnable)}..
 * @author heaven7
 */
public interface Consumer<T> {

    /**
     * called on consumers start
     * @param next the next run
     */
    void onStart(Runnable next);
    /**
     * consumers the object
     * @param obj the object
     * @param next the next run
     */
    void onConsume(T obj, Runnable next);

    /**
     * called on consumers end.
     */
    void onEnd();
}
