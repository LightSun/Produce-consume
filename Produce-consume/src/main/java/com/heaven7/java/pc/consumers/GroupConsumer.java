package com.heaven7.java.pc.consumers;

import com.heaven7.java.pc.Consumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * the group consumer which can wrap multi consumers
 * @param <T> the data type of consumer
 * @since 1.0.2
 */
public class GroupConsumer<T> implements Consumer<T> {

    private final Collection<Consumer<? super T>> mConsumers;

    public GroupConsumer(Consumer<? super T>...consumers){
        this(new ArrayList<Consumer<? super T>>(Arrays.asList(consumers)));
    }
    public GroupConsumer(Collection<Consumer<? super T>> mConsumers) {
        this.mConsumers = mConsumers;
    }

    @Override
    public void onStart(Runnable next) {
        GroupRunner gr = new GroupRunner(mConsumers.size(), next);
        for (Consumer<? super T> consumer: mConsumers){
            consumer.onStart(gr);
        }
    }
    @Override
    public void onConsume(T obj, Runnable next) {
        GroupRunner gr = new GroupRunner(mConsumers.size(), next);
        for (Consumer<? super T> consumer: mConsumers){
            consumer.onConsume(obj, gr);
        }
    }
    @Override
    public void onEnd() {
        for (Consumer<? super T> consumer: mConsumers){
            consumer.onEnd();
        }
    }

    private static class GroupRunner implements Runnable{
        final AtomicInteger mLeftCount;
        final Runnable core;

        public GroupRunner(int count, Runnable core) {
            this.mLeftCount = new AtomicInteger(count);
            this.core = core;
        }
        @Override
        public void run() {
            if(mLeftCount.decrementAndGet() == 0){
                core.run();
            }
        }
    }
}
