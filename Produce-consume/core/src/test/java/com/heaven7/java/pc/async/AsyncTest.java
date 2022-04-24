package com.heaven7.java.pc.async;

import com.heaven7.java.pc.ProductContext;
import com.heaven7.java.pc.Transformer;
import com.heaven7.java.pc.schedulers.Schedulers;
import org.junit.Test;

import java.util.List;

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

    @Test
    public void testList(){
        Asyncs.justMulti(1, 100)
                .everySize(8)
                .scheduler(Schedulers.io())
                .delay(1000)
                .transformer(new Transformer<List<Integer>, String>() {
                    @Override
                    public String transform(ProductContext context, List<Integer> list) {
                        StringBuilder sb = new StringBuilder();
                        for (Integer i : list) {
                            sb.append(i).append("_");
                        }
                        return sb.toString();
                    }
                }, true)
                .exception(new Action<Throwable>() {
                    @Override
                    public void run(Response<Throwable> result) {
                        result.getThrowable().printStackTrace();
                    }
                })
                .then(new Action<List<Object>>() {
                    @Override
                    public void run(Response<List<Object>> result) {
                        for (Object obj : result.getData()){
                            System.out.println(obj.toString());
                        }
                        release();
                    }
                }, Schedulers.single());
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
