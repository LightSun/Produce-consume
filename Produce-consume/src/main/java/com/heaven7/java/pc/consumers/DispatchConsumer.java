package com.heaven7.java.pc.consumers;

import com.heaven7.java.pc.Consumer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author heaven7
 */
public class DispatchConsumer<T> implements Consumer<T> {

    private final ArrayList<T> products = new ArrayList<>();

    @Override
    public void onStart(Runnable next) {
        next.run();
    }

    @Override
    public void onConsume(T obj, Runnable next) {
        products.add(obj);
        next.run();
    }

    @Override @SuppressWarnings("unchecked")
    public void onEnd() {
        ArrayList<T> list = new ArrayList<>(products);
        products.clear();
        fire(list);
    }
    protected void fire(List<T> products) {

    }
}
