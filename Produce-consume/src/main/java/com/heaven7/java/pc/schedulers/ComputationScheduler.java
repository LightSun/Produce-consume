package com.heaven7.java.pc.schedulers;

import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.base.util.threadpool.Executors2;
import com.heaven7.java.pc.internal.Config;

import java.util.concurrent.ThreadFactory;

/**
 * the computation scheduler which used a fixed size thread pool.
 * @author heaven7
 */
public final class ComputationScheduler extends CommonScheduler implements Scheduler {

    private static final int THREAD_COUNT;
    private static final ThreadFactory sFACTORY;

    static {
        int priority = Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY,
                Integer.getInteger(Config.KEY_COMPUTATION_PRIORITY, Thread.NORM_PRIORITY)));
        sFACTORY = new PCThreadFactory(priority, true);

        final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
        Integer count = Integer.getInteger(Config.KEY_COMPUTATION_THREAD_COUNT);
        THREAD_COUNT = count != null ? cap(CPU_COUNT, count) : CPU_COUNT;
    }

    public ComputationScheduler() {
        super(Executors2.newFixedThreadPool(THREAD_COUNT, sFACTORY));
    }

    static int cap(int cpuCount, int paramThreads) {
        return paramThreads <= 0 || paramThreads > cpuCount ? cpuCount : paramThreads;
    }

}
