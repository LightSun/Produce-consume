package com.heaven7.java.pc.schedulers;

import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.base.util.Scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author heaven7
 */
/*public*/ class ScheduleExecutorScheduler implements Scheduler {

    private final AtomicReference<ScheduledExecutorService> mRefService;

    public ScheduleExecutorScheduler(ScheduledExecutorService service) {
        this.mRefService = new AtomicReference<>(service);
    }
    @Override
    public Worker newWorker() {
        return new ScheduledWorker(mRefService.get());
    }

    private static class ScheduledWorker implements Worker{

        private final ScheduledExecutorService service;

        public ScheduledWorker(ScheduledExecutorService service) {
            this.service = service;
        }
        @Override
        public Disposable schedule(Runnable task) {
            return new Disposable.FutureDisposable(service.submit(task));
        }
        @Override
        public Disposable scheduleDelay(Runnable task, long delay, TimeUnit unit) {
            return new Disposable.FutureDisposable(service.schedule(task, delay, unit));
        }
        @Override
        public Disposable schedulePeriodically(Runnable task, long initDelay, long period, TimeUnit unit) {
            return new Disposable.FutureDisposable(service.scheduleAtFixedRate(task, initDelay, period, unit));
        }
    }
}
