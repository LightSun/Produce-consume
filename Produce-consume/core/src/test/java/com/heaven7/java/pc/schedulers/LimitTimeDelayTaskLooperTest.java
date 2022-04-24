package com.heaven7.java.pc.schedulers;

import org.junit.Test;

/**
 * @author heaven7
 */
public class LimitTimeDelayTaskLooperTest extends BaseLooperTest {

    @Override
    protected DelayTaskLooper createLooper() {
        return new LimitTimeDelayTaskLooper(
                2000,
                false,
                new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("LimitTimeDelayTaskLooper: time reached now.");
                        markFinished();
                    }
                });
    }

    @Test
    public void testTimeReach(){
        mLooper.scheduleDelay(mService, new Runnable() {
            @Override
            public void run() {
                System.out.println("testTimeReach: delay task is runned.");
                markFinished();
            }
        }, 3000);
        waitFinish();
    }
}
