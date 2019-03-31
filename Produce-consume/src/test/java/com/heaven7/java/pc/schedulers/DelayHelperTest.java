package com.heaven7.java.pc.schedulers;

import com.heaven7.java.base.util.threadpool.Executors2;
import com.heaven7.java.pc.BaseTest;
import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class DelayHelperTest extends BaseTest {

    private final DelayHelper mHelper = new DelayHelper();
    private final ExecutorService mService = Executors2.newFixedThreadPool(1);
    private final Executor mExecutor = new Executor() {
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    };

    @Test
    public void test1(){
        mHelper.scheduleDelay(mService, new Runnable() {
            @Override
            public void run() {
                System.out.println("---------" + System.currentTimeMillis());
                markFinished();
            }
        }, 300);
        waitFinish();
        System.out.println("finished");
    }
}
