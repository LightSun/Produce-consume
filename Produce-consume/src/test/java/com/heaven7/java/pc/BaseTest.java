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
    private static AtomicBoolean sWait_Called = new AtomicBoolean(false);
    private static AtomicBoolean sNotify_Called = new AtomicBoolean(false);

    public static List<String> createTasks() {
        List<String> list = new ArrayList<>();
        for (int i = 0 ; i < 2000; i++){
            list.add("heaven7__" + i);
        }
        return list;
    }

    public static void waitFinish(){
        sWait_Called.compareAndSet(false, true);
        if(sNotify_Called.get()){
            reset();
            return;
        }
        try {
            synchronized (sLock) {
                sLock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void reset() {
        sWait_Called.set(false);
        sNotify_Called.set(false);
    }

    public static void markFinished(){
        final boolean waitCalled = sWait_Called.get();
        synchronized (sLock) {
            sLock.notifyAll();
        }
        sNotify_Called.compareAndSet(false, true);
        if(waitCalled){
            reset();
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
