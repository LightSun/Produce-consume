package com.heaven7.java.pc.schedulers;

import com.heaven7.java.base.util.Disposable;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * the delay task looper. which used to loop the all delayed tasks on background.
 * but execute the task on other thread.
 * @author heaven7
 */
public interface DelayTaskLooper {

    /**
     * schedule the task to the looper. and when the delay time reached. the executor will used to execute the task. if
     * the executor is {@linkplain ExecutorService},
     * it will use the {@linkplain ExecutorService#submit(Runnable)} to run the task. or else if it is canceled by {@linkplain Disposable}.
     * it will be remove from this looper.
     * @param executor the executor
     * @param task the real task which will run after delay
     * @param delayMills the delay in mills.
     * @return the disposable which can dispose this schedule.
     */
    Disposable scheduleDelay(Executor executor, Runnable task, long delayMills);

}
