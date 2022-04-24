package com.heaven7.java.pc.producers;

import com.heaven7.java.base.util.Predicates;
import com.heaven7.java.pc.Producer;

import java.util.Collection;

/**
 * the simple producer factory
 * @param <T> the product type.
 * @author heaven7
 */
public final class ProducerFactory<T> {

   private final ProducerFactory.Builder<T> builder;

    protected ProducerFactory(ProducerFactory.Builder<T> builder) {
        this.builder = builder;
    }

    public Producer<T> create(){
        CollectionProducer<T> producer = new CollectionProducer<>(builder.products);
        if(builder.ordered){
            producer.addFlags(Producer.FLAG_SCHEDULE_ORDERED);
        }
        producer.setExceptionHandleStrategy(builder.strategy);
        return producer;
    }

    public static class Builder<T> {
        private Producer.ExceptionHandleStrategy<T> strategy;
        private boolean ordered;
        private Collection<T> products;

        public Builder<T> ordered() {
            this.ordered = true;
            return this;
        }
        public Builder<T> exceptionHandleStrategy(Producer.ExceptionHandleStrategy<T> strategy) {
            this.strategy = strategy;
            return this;
        }
        public Builder<T> products(Collection<T> products) {
            this.products = products;
            return this;
        }
        public ProducerFactory<T> build() {
            if(Predicates.isEmpty(products)){
                throw new IllegalStateException();
            }
            return new ProducerFactory<T>(this);
        }
    }
}
