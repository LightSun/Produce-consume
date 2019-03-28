package com.heaven7.java.pc.schedulers;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author heaven7
 */
public class PCThreadFactory implements ThreadFactory {

    private static final AtomicInteger sPOOL_NUMBER = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final boolean daemon;
    private final int priority;

    public PCThreadFactory(int priority, boolean daemon) {
        this("PCThreadF", priority, daemon);
    }

    public PCThreadFactory(String prefix, int priority, boolean daemon) {
        this.daemon = daemon;
        this.priority = priority;
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        namePrefix = prefix + "-" + sPOOL_NUMBER.getAndIncrement() +
                "-thread-";
    }

    public Thread newThread(Runnable r) {
        PCThread t = new PCThread(group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        t.setDaemon(daemon);
        if (t.getPriority() != priority)
            t.setPriority(priority);
        return t;
    }

    private static class PCThread extends Thread{
        public PCThread() {
        }
        public PCThread(Runnable target) {
            super(target);
        }
        public PCThread(ThreadGroup group, Runnable target) {
            super(group, target);
        }
        public PCThread(String name) {
            super(name);
        }
        public PCThread(ThreadGroup group, String name) {
            super(group, name);
        }
        public PCThread(Runnable target, String name) {
            super(target, name);
        }
        public PCThread(ThreadGroup group, Runnable target, String name) {
            super(group, target, name);
        }
        public PCThread(ThreadGroup group, Runnable target, String name, long stackSize) {
            super(group, target, name, stackSize);
        }
    }
}
