package com.heaven7.java.pc.schedulers;

import org.junit.Test;

/**
 * @author heaven7
 */
public class LimitTimeDelayTaskLooperTest2 extends BaseLooperTest {

    @Override
    protected DelayTaskLooper createLooper() {
        return new LimitTimeDelayTaskLooper(
                2000,
                true,
                new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("LimitTimeDelayTaskLooper: time reached now.");
                        markFinished();
                    }
                });
    }

    @Test
    public void testDisposeImmediately(){
        mLooper.scheduleDelay(mService, new Runnable() {
            @Override
            public void run() {
                System.out.println("testTimeReach: delay task is runned.");
            }
        }, 3000);
        mLooper.scheduleDelay(mService, new Runnable() {
            @Override
            public void run() {
                mLooper.dispose();
                markFinished();
            }
        }, 100);
        waitFinish();
    }
}
