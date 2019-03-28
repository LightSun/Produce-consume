package com.heaven7.java.pc.schedulers;

import com.heaven7.java.base.util.Scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author heaven7
 */
public abstract class BaseExecutorServiceScheduler implements Scheduler {

    private final AtomicReference<ExecutorService> mService;

    public BaseExecutorServiceScheduler(ExecutorService service) {
        this.mService = new AtomicReference<>(service);
    }

    public ExecutorService getExecutorService(){
        return mService.get();
    }
}
