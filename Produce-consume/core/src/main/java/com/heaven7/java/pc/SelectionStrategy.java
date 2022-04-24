package com.heaven7.java.pc;

import java.util.Collection;

/**
 * the selection strategy
 * @author heaven7
 * @since 1.0.2
 */
public interface SelectionStrategy<T> {

    /**
     * select one Consumer from collection
     * @param product the product
     * @param candidateConsumers the candidate consumers
     * @return the correct consumer
     */
    Consumer<T> select(T product, Collection<Consumer<? super T>> candidateConsumers);
}
