package com.heaven7.java.pc.consumers;

import com.heaven7.java.pc.BaseTest;
import com.heaven7.java.pc.Consumer;
import com.heaven7.java.pc.schedulers.Schedulers;
import org.junit.Test;

public class GroupConsumerTest extends BaseTest {

    final GroupConsumer<String> mConsumers = new GroupConsumer<>(consumer1(), consumer2());

    @Test
    public void testOnStart(){
        mConsumers.onStart(new Runnable() {
            @Override
            public void run() {
                System.out.println("-------------- mConsumers "+ "testOnStart" + "-------- done !");
                markFinished();
            }
        });
        waitFinish();
    }

    @Test
    public void testOnConsume(){
        mConsumers.onConsume("aaa",new Runnable() {
            @Override
            public void run() {
                System.out.println("-------------- mConsumers "+ "testOnConsume" + "-------- done !");
                markFinished();
            }
        });
        waitFinish();
    }

    @Test
    public void testOnEnd(){
        mConsumers.onEnd();
    }

    private Consumer<String> consumer1(){
        return new Consumer<String>() {
            @Override
            public void onStart(Runnable next) {
                System.out.println("consumer1 >>> on start.");
                next.run();
            }
            @Override
            public void onConsume(String obj, Runnable next) {
                System.out.println("consumer1 >>> on onConsume: " + obj);
                next.run();
            }
            @Override
            public void onEnd() {
                System.out.println("consumer1 >>> on onEnd.");
            }
        };
    }
    private Consumer<String> consumer2(){
         return new RedirectConsumer<>(new Consumer<String>() {
             @Override
             public void onStart(Runnable next) {
                 System.out.println("consumer2: onStart. " );
                 next.run();
             }
             @Override
             public void onConsume(String obj, Runnable next) {
                 System.out.println("consumer2: onConsume -- " + obj);
                 next.run();
             }
             @Override
             public void onEnd() {
                 System.out.println("consumer2: onEnd. " );
             }
         }, Schedulers.io().newWorker());
    }
}
