package com.heaven7.java.pc.schedulers;

import com.heaven7.java.base.util.threadpool.Executors2;
import com.heaven7.java.pc.internal.Config;

/**
 * @author heaven7
 */
public final class IoScheduler extends ScheduleExecutorScheduler {

    private static final int CORE_SIZE;
    private static final PCThreadFactory sFACTORY;

    static {
        int priority = Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY,
                Integer.getInteger(Config.KEY_IO_PRIORITY, Thread.NORM_PRIORITY)));
        sFACTORY = new PCThreadFactory(priority, true);

        Integer coreSize = Integer.getInteger(Config.KEY_IO_CORE_SIZE);
        CORE_SIZE = coreSize != null ? Math.max(coreSize, 0) : 0;
    }

    public IoScheduler() {
        super(Executors2.newScheduledThreadPool(CORE_SIZE, sFACTORY));
    }

}
