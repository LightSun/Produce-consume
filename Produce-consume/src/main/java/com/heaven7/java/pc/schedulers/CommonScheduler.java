package com.heaven7.java.pc.schedulers;

import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.pc.internal.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * the common scheduler
 * @author heaven7
 */
public class CommonScheduler extends BaseExecutorServiceScheduler implements Scheduler {

    private static final DelayHelper HELPER = new DelayHelper();

    public CommonScheduler(ExecutorService service) {
        super(service);
    }

    @Override
    public Worker newWorker() {
        return new Worker(getExecutorService());
    }

    private static class Worker implements Scheduler.Worker{

        final ExecutorService mService;
        public Worker(ExecutorService mService) {
            this.mService = mService;
        }
        @Override
        public Disposable schedule(Runnable task) {
            return new Disposable.FutureDisposable(mService.submit(task));
        }
        @Override
        public Disposable scheduleDelay(Runnable task, long delay, TimeUnit unit) {
            return HELPER.scheduleDelay(mService, task, unit.toMillis(delay));
        }

        @Override
        public Disposable schedulePeriodically(Runnable task, long initDelay, long period, TimeUnit unit) {
            InternalTask internalTask = new InternalTask(mService, HELPER, task, period);
            return HELPER.scheduleDelay(mService, internalTask, unit.toMillis(initDelay));
        }
    }

    private static class InternalTask implements Runnable, Disposable{
        final AtomicBoolean mCancelled = new AtomicBoolean(false);
        final ExecutorService service;
        final DelayHelper worker;
        final Runnable task;
        final long period;
        Disposable realDispose;

        public InternalTask(ExecutorService service, DelayHelper worker, Runnable task, long period) {
            this.service = service;
            this.worker = worker;
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
            realDispose = worker.scheduleDelay(service, this, Math.max(0, period - cost));
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
