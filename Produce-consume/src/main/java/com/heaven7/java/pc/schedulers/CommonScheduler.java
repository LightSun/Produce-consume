package com.heaven7.java.pc.schedulers;

import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.base.util.Throwables;
import com.heaven7.java.pc.internal.Utils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * the common scheduler which used a {@linkplain DelayTaskLooper} to loop the delay tasks. and run the task on executor.
 * @author heaven7
 */
public class CommonScheduler implements Scheduler {

    /**
     * the default delay task looper .
     */
    private static final DelayHelper HELPER = new DelayHelper();

    private final AtomicReference<Executor> mExecutorRef;
    private final DelayTaskLooper mLooper;

    public CommonScheduler(DelayTaskLooper mLooper, Executor executor) {
        Throwables.checkNull(mLooper);
        this.mExecutorRef = new AtomicReference<>(executor);
        this.mLooper = mLooper;
    }
    public CommonScheduler(Executor executor) {
        this(HELPER, executor);
    }

    @Override
    public Worker newWorker() {
        return new Worker(mLooper, mExecutorRef.get());
    }

    private static class Worker implements Scheduler.Worker{

        final Executor mService;
        final DelayTaskLooper looper;

        public Worker(DelayTaskLooper looper, Executor executor) {
            this.mService = executor;
            this.looper = looper;
        }
        @Override
        public Disposable schedule(Runnable task) {
            if(mService instanceof ExecutorService){
                return new Disposable.FutureDisposable(((ExecutorService) mService).submit(task));
            }else {
                WrapTask wrapTask = new WrapTask(task);
                mService.execute(wrapTask);
                return wrapTask;
            }
        }
        @Override
        public Disposable scheduleDelay(Runnable task, long delay, TimeUnit unit) {
            return looper.scheduleDelay(mService, task, unit.toMillis(delay));
        }

        @Override
        public Disposable schedulePeriodically(Runnable task, long initDelay, long period, TimeUnit unit) {
            InternalTask internalTask = new InternalTask(mService, looper, task, period);
            return looper.scheduleDelay(mService, internalTask, unit.toMillis(initDelay));
        }
    }
    private static class WrapTask implements Disposable, Runnable{
        final AtomicBoolean mCancelled = new AtomicBoolean(false);
        final Runnable task;
        public WrapTask(Runnable task) {
            this.task = task;
        }
        @Override
        public void run() {
             if(mCancelled.get()){
                 return;
             }
            task.run();
        }
        @Override
        public void dispose() {
            mCancelled.compareAndSet(false, true);
        }
    }

    private static class InternalTask implements Runnable, Disposable{
        final AtomicBoolean mCancelled = new AtomicBoolean(false);
        final Executor service;
        final DelayTaskLooper looper;
        final Runnable task;
        final long period;
        Disposable realDispose;

        public InternalTask(Executor service, DelayTaskLooper looper, Runnable task, long period) {
            this.service = service;
            this.looper = looper;
            this.task = task;
            this.period = period;
        }
        @Override
        public void run() {
            final long start = Utils.currentTimeMillis();
            if(mCancelled.get()){
                return;
            }
            task.run();
            long cost = Utils.currentTimeMillis() - start;
            realDispose = looper.scheduleDelay(service, this, Math.max(0, period - cost));
        }
        @Override
        public void dispose() {
            if(mCancelled.compareAndSet(false, true)){
                if(realDispose != null){
                    realDispose.dispose();
                    realDispose = null;
                }
            }
        }
    }
}
