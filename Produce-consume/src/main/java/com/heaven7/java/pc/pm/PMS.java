package com.heaven7.java.pc.pm;

import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.pc.*;

/**
 * the product manager service.
 * @param <T> the raw product type
 * @param <R> the transformed product type.
 * @author heaven7
 */
public final class PMS<T, R> implements Disposable{

    private final ProductManager<T, R> pm;
    private final Consumer<? super R> consumer;

    private PMS(Builder<T, R> builder) {
        pm = new SimpleProductManager<>(builder.producer);
        pm.setProductContext(builder.context);
        pm.setScheduler(builder.scheduler);
        pm.setTransformer(builder.transformer);

        this.consumer = builder.consumer;
    }
    public boolean start(){
        return pm.open(this.consumer);
    }
    @Override
    public void dispose() {
        pm.close();
    }

    public static class Builder<T,R>{
        private ProductContext context;
        private Scheduler scheduler;
        private Transformer<? super T, R> transformer;
        private Consumer<? super R> consumer;
        private Producer<T> producer;

        public Builder<T,R> context(ProductContext context){
            this.context = context;
            return this;
        }
        public Builder<T,R> scheduler(Scheduler scheduler){
            this.scheduler = scheduler;
            return this;
        }
        public Builder<T,R> transformer(Transformer<? super T, R> transformer){
            this.transformer = transformer;
            return this;
        }
        public Builder<T,R> consumer(Consumer<? super R> consumer){
            this.consumer = consumer;
            return this;
        }
        public Builder<T,R> producer(Producer<T> producer){
            this.producer = producer;
            return this;
        }
        public PMS<T, R> build(){
            if(scheduler == null || transformer == null || consumer == null || producer == null){
                throw new IllegalStateException();
            }
            return new PMS<T, R>(this);
        }
    }
}
