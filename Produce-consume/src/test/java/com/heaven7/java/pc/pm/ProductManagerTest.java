package com.heaven7.java.pc.pm;

import com.heaven7.java.pc.*;
import com.heaven7.java.pc.consumers.DispatchConsumer;
import com.heaven7.java.pc.producers.CollectionProducer;
import com.heaven7.java.pc.producers.IterableProducer;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author heaven7
 */
public class ProductManagerTest extends BaseTest{

    @Test
    public void testNoOrder(){
        CollectionProducer<String> producer = new CollectionProducer<>(sTasks);
        testImpl(producer);
    }
    @Test
    public void testNoOrder2(){
        IterableProducer<String> producer = new IterableProducer<>(sTasks);
        testImpl(producer);
    }
    @Test
    public void testInOrder(){
        CollectionProducer<String> producer = new CollectionProducer<>(sTasks);
        producer.addFlags(Producer.FLAG_SCHEDULE_ORDERED);
        testImpl(producer);
    }
    @Test
    public void testInOrder2(){
        IterableProducer<String> producer = new IterableProducer<>(sTasks);
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
        CollectionProducer<String> producer = new CollectionProducer<>(sTasks);
        producer.addFlags(Producer.FLAG_SCHEDULE_ORDERED);
        producer.setExceptionHandleStrategy(strategy);

        DirectProductManager<String> source = new DirectProductManager<>(producer);
        source.setScheduler(TestSchedulers.GROUP_ASYNC);

        source.open(new ExceptionConsumer<>());
        waitFinish();
    }
    @Test
    public void testCancel(){
        CollectionProducer<String> producer = new CollectionProducer<>(sTasks);
        producer.addFlags(Producer.FLAG_SCHEDULE_ORDERED);

        final DirectProductManager<String> source = new DirectProductManager<>(producer);
        source.setScheduler(TestSchedulers.GROUP_ASYNC);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    source.close();
                    BaseProducer pro = (BaseProducer) source.producer;
                    pro.assertTaskIsEmpty();
                    markFinished();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        source.open(new DispatchConsumer<>());
        waitFinish();
    }

    protected void testImpl(Producer<String> producer){
        SimpleProductManager<String,String> source = new SimpleProductManager<>(producer);
        source.setScheduler(TestSchedulers.GROUP_ASYNC);
        source.setTransformer(Transformers.<String>unchangeTransformer());

        source.open(new TestConsumer<String>(producer.hasFlags(Producer.FLAG_SCHEDULE_ORDERED)));
        waitFinish();
    }
    private static class ExceptionConsumer<T> extends DispatchConsumer<T>{
        @Override
        public void onConsume(T obj, Runnable next) {
            throw new RuntimeException(MSG);
        }
    }

    private static class TestConsumer<T> extends DispatchConsumer<T> {

        final boolean inOrder;

        public TestConsumer(boolean inOrder) {
            this.inOrder = inOrder;
        }

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
           // new Throwable().printStackTrace();
            super.onEnd();
        }

        @Override
        protected void fire(List<T> products) {
            System.out.println("size = " + products.size());
            System.out.println(products);
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
}
