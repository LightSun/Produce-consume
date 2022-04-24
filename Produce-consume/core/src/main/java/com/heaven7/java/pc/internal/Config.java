package com.heaven7.java.pc.internal;

/**
 * @author heaven7
 */
public abstract class Config {

    public static final String KEY_COMPUTATION_PRIORITY      = "pc.scheduler.compute.priority";
    public static final String KEY_COMPUTATION_THREAD_COUNT  = "pc.scheduler.compute.thread-count";

    public static final String KEY_IO_PRIORITY               = "pc.scheduler.io.priority";
    public static final String KEY_IO_CORE_SIZE              = "pc.scheduler.io.core-size";
    public static final String KEY_TIME_LIMIT_LOOP_KEEP_TIME = "pc.scheduler.time-limit.loop.keep-time"; //in seconds
    public static final String KEY_DELAY_QUEUE_SIZE          = "pc.scheduler.delay.loop.queue-size";
}
