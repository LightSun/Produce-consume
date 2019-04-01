package com.heaven7.java.pc.schedulers;

import com.heaven7.java.base.util.Scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;


/**
 * the scheduler factory. which can create multi type schedulers.
 * @author heaven7
 */
public final class Schedulers {

    private static class Creator{
        static final AtomicReference<Scheduler> REF_IO = new AtomicReference<>();
        static final AtomicReference<Scheduler> REF_COMPUTE = new AtomicReference<>();
        static final AtomicReference<Scheduler> REF_SINGLE = new AtomicReference<>();
    }
    public static Scheduler io(){
        AtomicReference<Scheduler> refIo = Creator.REF_IO;
        refIo.compareAndSet(null, new IoScheduler());
        return refIo.get();
    }
    public static Scheduler compute(){
        AtomicReference<Scheduler> refIo = Creator.REF_COMPUTE;
        refIo.compareAndSet(null, new ComputationScheduler());
        return refIo.get();
    }
    public static Scheduler single(){
        AtomicReference<Scheduler> refIo = Creator.REF_SINGLE;
        refIo.compareAndSet(null, new SingleThreadScheduler());
        return refIo.get();
    }
    public static Scheduler scheduleExecutor(ScheduledExecutorService service){
        return new ScheduleExecutorScheduler(service);
    }
    public static Scheduler common(DelayTaskLooper looper, Executor executor){
        return new CommonScheduler(looper, executor);
    }
    public static Scheduler common(Executor executor){
        return new CommonScheduler(executor);
    }
}
