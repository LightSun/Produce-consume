package com.heaven7.java.pc;

import com.heaven7.java.pc.consumers.DispatchConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author heaven7
 */
public class BaseTest {

    public static final  String MSG = "test ExceptionConsumer__@";
    protected static final List<String> sTasks = createTasks();
    private static final Object sLock = new Object();

    public static List<String> createTasks() {
        return createTasks(200);
    }
    public static List<String> createTasks(int count) {
        List<String> list = new ArrayList<>();
        for (int i = 0 ; i < count; i++){
            list.add("heaven7__" + i);
        }
        return list;
    }

    public static void waitFinish(){
        try {
            synchronized (sLock) {
                sLock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void markFinished(){
        synchronized (sLock) {
            sLock.notifyAll();
        }
    }
    public static class ExceptionConsumer<T> extends DispatchConsumer<T> {
        static String MSG = "test ExceptionConsumer__@";
        @Override
        public void onConsume(T obj, Runnable next) {
            throw new RuntimeException(MSG);
        }
    }
}
