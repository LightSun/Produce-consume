package com.heaven7.java.pc.schedulers;

import com.heaven7.java.base.util.Disposable;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class SingleThreadSchedulerTest extends BaseSchedulerTest {

    @Override
    protected void setUp() {
        super.setUp();
        mScheduler = Schedulers.single();
    }

    @Test
    public void testRun_cancel_add(){
        final Disposable dispose = mScheduler.newWorker().schedulePeriodically(new Task("1"), 0, 1, TimeUnit.SECONDS);

        mScheduler.newWorker().scheduleDelay(new Runnable() {
            @Override
            public void run() {
                dispose.dispose();
                System.err.println("------- disposed ------");
                markFinished();

            }
        }, 3, TimeUnit.SECONDS);
        waitFinish();
    }

    private static class Task implements Runnable{

        final String tag;

        public Task(String tag) {
            this.tag = tag;
        }
        @Override
        public void run() {
            System.out.println(tag + " >>> schedulePeriodically: " + System.currentTimeMillis());
        }
    }

}
