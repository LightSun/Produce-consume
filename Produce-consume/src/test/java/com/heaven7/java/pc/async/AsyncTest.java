package com.heaven7.java.pc.async;

import com.heaven7.java.pc.ProductContext;
import com.heaven7.java.pc.Transformer;
import com.heaven7.java.pc.schedulers.Schedulers;
import org.junit.Test;

public class AsyncTest {

    @Test
    public void test1() {
        Async.<Integer, String>just(1)
                .scheduler(Schedulers.compute())
                .delay(1000)
                .transformer(new Transformer<Object, String>() {
                    @Override
                    public String transform(ProductContext context, Object integer) {
                        return integer + "__heaven7";
                    }
                }).then(new Action<String>() {
            @Override
            public void run(Response<String> result) {
                System.out.println(result);
                release();
            }
        });
        waitFor();
    }

    private void waitFor() {
        try {
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void release() {
        synchronized (this) {
            notifyAll();
        }
    }
}
