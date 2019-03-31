package com.heaven7.java.pc;

import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.pc.internal.Exceptions;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * the cancelable task used to wrap the real task and callback.
 * @author heaven7
 */
public final class CancelableTask implements Disposable {

    public static final CancelableTask CANCELLED = new CancelableTask(null, null);

    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final Runnable task;
    private final Callback callback;

    private Producer.Params params;
    private Disposable disposable;

    public interface Callback {

        void onTaskPlan(CancelableTask wrapTask);

        void onTaskBegin(CancelableTask wrapTask);

        void onTaskEnd(CancelableTask wrapTask, boolean cancelled);

        void onException(CancelableTask wrapTask, RuntimeException e);
    }

    static {
        CANCELLED.cancel();
    }

    private CancelableTask(Runnable task, Callback callback) {
        this.task = task;
        this.callback = callback;
    }

    public static CancelableTask of(Runnable task, Callback callback) {
        return new CancelableTask(task, callback);
    }

    @Override
    public void dispose() {
        cancel();
    }

    public void reset() {
        if (this.params != null) {
            this.params = null;
        }
        if (disposable != null) {
            disposable = null;
        }
    }

    public void setDisposable(Disposable disposable) {
        this.disposable = disposable;
    }

    public void setProduceParams(Producer.Params params) {
        this.params = params;
    }

    public Producer.Params getProduceParams() {
        return params;
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public boolean cancel() {
        if (cancelled.compareAndSet(false, true)) {
            if (disposable != null) {
                disposable.dispose();
                disposable = null;
            }
            return true;
        }
        return false;
    }

    public Runnable toActuallyTask() {
        callback.onTaskPlan(this);
        return new InternalTask(this);
    }

    private static class InternalTask implements Runnable {
        final CancelableTask wrapTask;

        public InternalTask(CancelableTask task) {
            this.wrapTask = task;
        }
        @Override
        public void run() {
            Callback callback = wrapTask.callback;
            try {
                callback.onTaskBegin(wrapTask);
                boolean cancelled = wrapTask.isCancelled();
                if (!cancelled) {
                    wrapTask.task.run();
                }
                callback.onTaskEnd(wrapTask, cancelled);
            } catch (Throwable e) {
                callback.onException(wrapTask, Exceptions.cast(e));
            } finally {
                wrapTask.reset();
            }
        }
    }

}
