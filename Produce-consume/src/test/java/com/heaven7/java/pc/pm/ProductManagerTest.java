package com.heaven7.java.pc.pm;

import com.heaven7.java.pc.Producer;
import com.heaven7.java.pc.Schedulers;
import com.heaven7.java.pc.Transformers;
import com.heaven7.java.pc.consumers.DispatchConsumer;
import com.heaven7.java.pc.producers.CollectionProducer;
import com.heaven7.java.pc.producers.IterableProducer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author heaven7
 */
public class ProductManagerTest {

    @Test
    public void testNoOrder(){
        CollectionProducer<String> producer = new CollectionProducer<>(createTasks());
        testImpl(producer);
    }
    @Test
    public void testNoOrder2(){
        IterableProducer<String> producer = new IterableProducer<>(createTasks());
        testImpl(producer);
    }
    @Test
    public void testInOrder(){
        CollectionProducer<String> producer = new CollectionProducer<>(createTasks());
        producer.addFlags(Producer.FLAG_SCHEDULE_ORDERED);
        testImpl(producer);
    }
    @Test
    public void testInOrder2(){
        IterableProducer<String> producer = new IterableProducer<>(createTasks());
        producer.addFlags(Producer.FLAG_SCHEDULE_ORDERED);
        testImpl(producer);
    }

    protected void testImpl(Producer<String> producer){
        SimpleProductManager<String,String> source = new SimpleProductManager<>(producer);
        source.setScheduler(Schedulers.GROUP_ASYNC);
        source.setTransformer(Transformers.<String>unchangeTransformer());

        source.open(new TestConsumer<String>());
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static List<String> createTasks() {
        List<String> list = new ArrayList<>();
        for (int i = 0 ; i < 50; i++){
            list.add("heaven7__" + i);
        }
        return list;
    }

    private static class TestConsumer<T> extends DispatchConsumer<T> {
        @Override
        public void onStart(Runnable next) {
            System.out.println("start -------");
            super.onStart(next);
        }
        @Override
        public void onConsume(T obj, Runnable next) {
            System.out.println("onConsume: " + obj);
            super.onConsume(obj, next);
        }

        @Override
        public void onEnd() {
            System.out.println("end -------");
            super.onEnd();
        }

        @Override
        protected void fire(List<T> products) {
            System.out.println("size = " + products.size());
            System.out.println(products);
        }
    }
}
