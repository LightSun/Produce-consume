package com.heaven7.java.pc.schedulers;

import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.base.util.Scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * the async executor
 * @param <V> the result type
 * @since 1.0.5
 */
public final class AsyncExecutor<V> {

    private final Callable<V> callable;
    private long delay;
    private Scheduler scheduler;

    private Future<V> future;

    public AsyncExecutor(Callable<V> callable) {
        this.callable = callable;
    }
    public AsyncExecutor<V> delay(long delay){
        this.delay = delay;
        return this;
    }
    public AsyncExecutor<V> scheduler(Scheduler scheduler){
        this.scheduler = scheduler;
        return this;
    }
    public Future<V> future(){
        return new Future<>(callable);
    }
    public AsyncExecutor<V> then(Action<V> task){
        if(future == null){
            future = future();
        }
        future.then(task);
        return this;
    }
    public AsyncExecutor<V> exception(Action<Throwable> task){
        if(future == null){
            future = future();
        }
        future.exception(task);
        return this;
    }
    public Disposable schedule(Scheduler observe){
        if(future == null){
            throw new IllegalStateException();
        }
        return future.schedule(getScheduler(), delay, observe);
    }
    private Scheduler getScheduler(){
        return scheduler != null ? scheduler : Schedulers.io();
    }
    public static class Future<V> implements Disposable{

        private Disposable scheduleDispose;
        private Disposable observeDispose;
        private final Callable<V> actTask;

        private Action<V> resultTask;
        private Action<Throwable> exceptionTask;

        private Future(Callable<V> actTask) {
            this.actTask = actTask;
        }
        public Future<V> then(Action<V> task){
            resultTask = task;
            return this;
        }
        public Future<V> exception(Action<Throwable> task){
            exceptionTask = task;
            return this;
        }

        @Override
        public void dispose() {
            if(scheduleDispose != null){
                scheduleDispose.dispose();
                scheduleDispose = null;
            }
            if(observeDispose != null){
                observeDispose.dispose();
                observeDispose = null;
            }
        }
        public Future<V> schedule(Scheduler scheduler, long delay, final Scheduler observe){
            Runnable wrapTask = new Runnable() {
                @Override
                public void run() {
                    run0(observe);
                }
            };
            final Disposable d;
            if(delay > 0){
                d = scheduler.newWorker().schedule(wrapTask);
            }else {
                d = scheduler.newWorker().scheduleDelay(wrapTask, delay, TimeUnit.MILLISECONDS);
            }
            this.scheduleDispose = d;
            return this;
        }
        private void run0(Scheduler observe){
            V result = null;
            Exception exp = null;
            if(actTask != null){
                try {
                    result = actTask.call();
                } catch (Exception e) {
                    exp = e;
                }
            }
            if(observe == null){
                callback(result, exp);
            }else {
                final V result1 = result;
                final Exception exp1 = exp;
                observeDispose = observe.newWorker().schedule(new Runnable() {
                    @Override
                    public void run() {
                        callback(result1, exp1);
                    }
                });
            }
        }
        private void callback(V result, Throwable e0){
            if(exceptionTask != null){
                exceptionTask.run(e0);
            }else{
                try {
                    resultTask.run(result);
                }catch (Throwable e){
                    exceptionTask.run(e);
                }
            }
        }
    }
    public interface Action<V>{
        void run(V result);
    }
}
