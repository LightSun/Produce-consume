package com.heaven7.java.pc.producers;

import com.heaven7.java.pc.Producer;

import java.util.Collection;

/**
 * @author heaven7
 */
public class CollectionProducerTest extends IterableProducerTest {

    @Override
    protected <T> Producer<T> createProducer(Collection<T> tasks) {
        return new CollectionProducer<>(tasks);
    }
}
