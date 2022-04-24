package com.heaven7.java.pc.schedulers;

import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.base.util.threadpool.Executors2;
import com.heaven7.java.pc.BaseTest;
import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * @author heaven7
 */
public abstract class BaseLooperTest extends BaseTest{

    protected DelayTaskLooper mLooper;
    protected final ExecutorService mService = Executors2.newFixedThreadPool(1);
    protected final Executor mExecutor = new Executor() {
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    };

    public BaseLooperTest() {
        this.mLooper = createLooper();
    }
    protected abstract DelayTaskLooper createLooper();

    @Test
    public void testOnService(){
        mLooper.scheduleDelay(mService, new Runnable() {
            @Override
            public void run() {
                System.out.println("---------" + System.currentTimeMillis());
                markFinished();
            }
        }, 300);
        waitFinish();
        System.out.println("finished");
    }

    @Test
    public void testOnExecutor(){
        mLooper.scheduleDelay(mExecutor, new Runnable() {
            @Override
            public void run() {
                System.out.println("---------" + System.currentTimeMillis());
                markFinished();
            }
        }, 300);
        waitFinish();
        System.out.println("finished");
    }

    @Test
    public void testDispose(){
        Disposable task = mLooper.scheduleDelay(mExecutor, new Runnable() {
            @Override
            public void run() {
                System.out.println("---------" + System.currentTimeMillis());
            }
        }, 300);
        task.dispose();
        System.out.println("disposed << ");
    }
}
