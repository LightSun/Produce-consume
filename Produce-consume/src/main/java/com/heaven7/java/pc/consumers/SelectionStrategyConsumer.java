package com.heaven7.java.pc.consumers;

import com.heaven7.java.pc.Consumer;
import com.heaven7.java.pc.SelectionStrategy;

import java.util.Collection;

/**
 *  a group consumer which consume product by a selection strategy.
 * @param <T> the product type
 * @since 1.0.2
 */
public class SelectionStrategyConsumer<T> extends GroupConsumer<T> {

    private final SelectionStrategy<T> selectionStrategy;

    public SelectionStrategyConsumer(Collection<Consumer<? super T>> mConsumers, SelectionStrategy<T> selectionStrategy) {
        super(mConsumers);
        this.selectionStrategy = selectionStrategy;
    }

    @Override
    public void onConsume(T obj, Runnable next) {
        Consumer<T> consumer = selectionStrategy.select(obj, mConsumers);
        if(consumer != null){
            consumer.onConsume(obj, next);
        }else {
            next.run();
        }
    }
}
