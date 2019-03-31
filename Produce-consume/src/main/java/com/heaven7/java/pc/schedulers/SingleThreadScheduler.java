package com.heaven7.java.pc.schedulers;

import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.base.util.threadpool.Executors2;

/**
 * single thread scheduler
 * @author heaven7
 */
public class SingleThreadScheduler extends CommonScheduler implements Scheduler {

    public SingleThreadScheduler() {
        super(Executors2.newSingleThreadExecutor());
    }
}
