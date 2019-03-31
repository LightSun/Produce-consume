package com.heaven7.java.pc.schedulers;

import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.pc.BaseTest;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class BaseSchedulerTest extends BaseTest {

    protected Scheduler mScheduler;

    public BaseSchedulerTest() {
        setUp();
    }

    protected void setUp() {
        mScheduler = new ComputationScheduler();
    }

    @Test
    public void test1(){
        mScheduler.newWorker().schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println("test1");
            }
        });
    }

    @Test
    public void testDelay1(){
        Disposable task = mScheduler.newWorker().scheduleDelay(new Runnable() {
            @Override
            public void run() {
                throw new UnsupportedOperationException("dispose failed");
            }
        }, 3, TimeUnit.SECONDS);
        task.dispose();
    }
    @Test
    public void testDelay2(){
        final long startTime = System.currentTimeMillis();
        mScheduler.newWorker().scheduleDelay(new Runnable() {
            @Override
            public void run() {
                System.out.println("testDelay2: cost = " + (System.currentTimeMillis() - startTime));
                markFinished();
            }
        }, 800, TimeUnit.MILLISECONDS);
        waitFinish();
    }
    @Test
    public void testPeriod1(){
        mScheduler.newWorker().schedulePeriodically(new Runnable() {
            int count;
            long start = System.currentTimeMillis();
            @Override
            public void run() {
                long end = System.currentTimeMillis();
                System.out.println("testDelay2: cost = " + (end - start) + " ,count = " + count);
                start = end;
                if(count ++ >= 10) {
                    markFinished();
                }
            }
        }, 300,300, TimeUnit.MILLISECONDS);
        waitFinish();
    }
    @Test
    public void testPeriod2(){
        final Disposable task = mScheduler.newWorker().schedulePeriodically(new Runnable() {
            @Override
            public void run() {
                throw new UnsupportedOperationException("dispose failed");
            }
        }, 3000, 3000, TimeUnit.MILLISECONDS);
        mScheduler.newWorker().scheduleDelay(new Runnable() {
            @Override
            public void run() {
                task.dispose();
            }
        }, 500, TimeUnit.MILLISECONDS);
    }
}
