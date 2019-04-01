package com.heaven7.java.pc.producers;

import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.pc.BaseProducer;
import com.heaven7.java.pc.ProductContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author heaven7
 */
public class PipeProducer<T> extends BaseProducer<T> {

    public interface Pipe<T>{
        void addProduct(T t);
        void close();
    }
    private final AtomicBoolean mPipClosed = new AtomicBoolean(false);
    private final Pipe0<T> mPipe = new Pipe0<T>(this);

    private ProductContext context;
    private Scheduler scheduler;
    private Callback<T> callback;

    @Override
    protected void produceOrdered(ProductContext context, Scheduler scheduler, Callback<T> callback) {
        setInternal(context, scheduler, callback);
        if(mPipe.hasPendingProducts()){

        }
    }
    @Override
    protected void produce0(ProductContext context, Scheduler scheduler, Callback<T> callback) {
        setInternal(context, scheduler, callback);
    }

    public Pipe<T> getPipe(){
        return mPipe;
    }

    private void setInternal(ProductContext context, Scheduler scheduler, Callback<T> callback){
        this.context = context;
        this.scheduler = scheduler;
        this.callback = callback;
    }
    boolean isPrepared(){
        return callback != null;
    }

    private static class Pipe0<T> implements Pipe<T>{

        final PipeProducer<T> producer;
        final List<T> pendings = new ArrayList<>();

        public Pipe0(PipeProducer<T> producer) {
            this.producer = producer;
        }
        @Override
        public void addProduct(T t) {
            if(producer.isPrepared()){

            }else {
                pendings.add(t);
            }
        }
        @Override
        public void close() {

        }
        public boolean hasPendingProducts() {
            return pendings.size() > 0;
        }
    }
}

