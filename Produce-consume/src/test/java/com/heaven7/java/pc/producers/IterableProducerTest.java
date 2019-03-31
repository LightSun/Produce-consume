package com.heaven7.java.pc.producers;

import com.heaven7.java.pc.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author heaven7
 */
public class IterableProducerTest extends BaseTest{


    protected <T>Producer<T> createProducer(Collection<T> tasks){
        return new IterableProducer<>(tasks);
    }

    @Test
    public void testNoOrder(){
        testImpl(createProducer(sTasks));
    }
    @Test
    public void testInOrder(){
        Producer<String> producer = createProducer(sTasks);
        producer.addFlags(Producer.FLAG_SCHEDULE_ORDERED);
        testImpl(producer);
    }
    @Test
    public void testException(){
        Producer.ExceptionHandleStrategy<String> strategy = new Producer.ExceptionHandleStrategy<String>() {
            @Override
            public void handleException(Producer<String> producer, Producer.Params params, RuntimeException e) {
                //DefaultPrinter.getDefault().error("testException", "handleException", e);
                Assert.assertTrue(MSG.equals(e.getMessage()));
                markFinished();
            }
        };
        Producer<String> producer = createProducer(sTasks);
        producer.addFlags(Producer.FLAG_SCHEDULE_ORDERED);
        producer.setExceptionHandleStrategy(strategy);
        testImpl(producer, new ExceptionCallback());
    }
    @Test
    public void testClose(){
        final Producer<String> producer = createProducer(sTasks);
        producer.addFlags(Producer.FLAG_SCHEDULE_ORDERED);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    producer.close();
                    ((BaseProducer)producer).assertTaskIsEmpty();
                    markFinished();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        testImpl(producer, new SimpleCallback());
    }

    private void testImpl(Producer<String> producer, Producer.Callback<String> callback) {
        producer.open();
        producer.produce(sContext, TestSchedulers.GROUP_ASYNC,  callback);
        waitFinish();
    }

    private void testImpl(Producer<String> producer) {
        producer.open();
        producer.produce(sContext, TestSchedulers.GROUP_ASYNC,  new Callback0(producer.hasFlags(Producer.FLAG_SCHEDULE_ORDERED)));
        waitFinish();
    }
    private static class ExceptionCallback implements Producer.Callback<String>{

        @Override
        public void onStart(ProductContext context, Runnable next) {
            next.run();
        }
        @Override
        public void onProduced(ProductContext context, String s, Runnable next) {
            throw new UnsupportedOperationException(MSG);
        }
        @Override
        public void onEnd(ProductContext context) {

        }
    }
    private static class SimpleCallback implements Producer.Callback<String>{
        @Override
        public void onStart(ProductContext context, Runnable next) {
            next.run();
        }
        @Override
        public void onProduced(ProductContext context, String s, Runnable next) {
            next.run();
        }
        @Override
        public void onEnd(ProductContext context) {

        }
    }

    private static class Callback0 implements Producer.Callback<String>{
        final List<String> products = new ArrayList<>();
        final boolean inOrder;

        public Callback0(boolean inOrder) {
            this.inOrder = inOrder;
        }
        @Override
        public void onStart(ProductContext context, Runnable next) {
            System.out.println("onStart ---");
            Assert.assertTrue(context == sContext);
            next.run();
        }
        @Override
        public void onProduced(ProductContext context, String o, Runnable next) {
            Assert.assertTrue(context == sContext);
            products.add(o);
            next.run();
        }
        @Override
        public void onEnd(ProductContext context) {
            System.out.println("onEnd ---");
            Assert.assertTrue(context == sContext);
            if(inOrder){
                Assert.assertTrue(sTasks.equals(products));
            }else {
                for (String task : sTasks){
                    Assert.assertTrue(products.contains(task));
                }
            }
            markFinished();
        }
    }

    private static final ProductContext sContext = new ProductContext() {
    };
}
