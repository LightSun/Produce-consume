package com.heaven7.java.pc.schedulers;

import com.heaven7.java.base.util.DefaultPrinter;
import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.pc.internal.Config;
import com.heaven7.java.pc.internal.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * the delay helper which handle the delay task and run on the executor.
 * note after you called {@linkplain DelayHelper#dispose()}. the looper will have nothing effect.
 * @author heaven7
 */
public class DelayHelper implements DelayTaskLooper, Runnable, Disposable {

    /**
     * the default delay task looper .
     */
    public static final DelayHelper DEFAULT;

    private static final int QUEUE_SIZE;

    static {
        Integer size = Integer.getInteger(Config.KEY_DELAY_QUEUE_SIZE, null);
        QUEUE_SIZE = size != null && size > 0 ? size : 128;

        DEFAULT = new DelayHelper();
    }

    private static final String TAG = "DelayHelper";

    private final AtomicBoolean mCancelled = new AtomicBoolean(false);
    private final BlockingQueue<Task> mQueue;
    private final List<Task> mList = new ArrayList<>();
    private final Thread mThread;
    private final boolean mDisposeImmediately;

    public DelayHelper(){
        this(true);
    }
    public DelayHelper(boolean disposeImmediately) {
        this.mQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);

        this.mDisposeImmediately = disposeImmediately;
        this.mThread = new Thread(this);
        this.mThread.setDaemon(true);
        this.mThread.start();
    }

    @Override
    public Disposable scheduleDelay(Executor executor, Runnable task, long delay) {
        Task realTask = new Task(task, delay, executor);
        mQueue.offer(realTask);
        synchronized (this) {
            this.notify();
        }
        return realTask;
    }

    @Override
    public void run() {
        try {
            onStartLoop();
            while (!shouldExitLoop()) {
                mList.addAll(mQueue);
                final int count = mList.size();
                if (count == 0) {
                    synchronized (this) {
                        this.wait();
                    }
                    continue;
                }
                for (int i = 0; i < count; i++) {
                    Task task = mList.get(i);
                    if (!task.run()) {
                        mQueue.remove(task);
                    }
                }
                mList.clear();
            }
            onEndLoop();
        } catch (InterruptedException e) {
            // ignore
        }finally{
            mQueue.clear();
            mList.clear();
        }
    }

    @Override
    public void dispose() {
        if (mCancelled.compareAndSet(false, true)) {
            if (mDisposeImmediately) {
                mThread.interrupt();
            }
        }
    }

    public boolean isCancelled(){
        return mCancelled.get();
    }

    /**
     * called on start loop
     */
    protected void onStartLoop() {}

    /**
     * called on end loop.
     */
    protected void onEndLoop() {}

    /**
     * check if need exit the loop. default impl is check cancelled or not.
     * @return true if should exit loop.
     */
    protected boolean shouldExitLoop() {
        return mCancelled.get();
    }
    private static class Task implements Disposable {
        final Runnable task;
        final long targetTime;
        final SmartExecutor smartExecutor;

        public Task(Runnable task, long delay, Executor realService) {
            this.smartExecutor = new SmartExecutor(realService);
            this.task = task;
            this.targetTime = Utils.currentTimeMillis() + delay;
        }
        // run the task at current time. if task need remove . return false.
        public boolean run() {
            if (Utils.currentTimeMillis() < targetTime) {
                return true;
            }
            return smartExecutor.execute(task);
        }

        @Override
        public void dispose() {
            smartExecutor.dispose();
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Task task1 = (Task) o;
            return Objects.equals(task, task1.task);
        }
        @Override
        public int hashCode() {
            return Objects.hash(task);
        }
    }

    private static class SmartExecutor implements Disposable {
        final AtomicBoolean mCancelled = new AtomicBoolean(false);
        private Executor executor;
        private WeakReference<ExecutorService> weakExecutor;
        Disposable realDispose;

        public SmartExecutor(Executor executor) {
            if (executor instanceof ExecutorService) {
                weakExecutor = new WeakReference<ExecutorService>((ExecutorService) executor);
            } else {
                this.executor = executor;
            }
        }
        // return false. means task will be remove.
        public boolean execute(final Runnable task) {
            if (mCancelled.get()) {
                return false;
            }
            if (executor == null) {
                ExecutorService service = weakExecutor.get();
                if (service != null) {
                    realDispose = new FutureDisposable(service.submit(task));
                } else {
                    DefaultPrinter.getDefault().error(TAG, "execute", "scheduler does not exist .");
                }
            } else {
                executor.execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (!mCancelled.get()) {
                                    task.run();
                                }
                            }
                        });
            }
            return false;
        }

        @Override
        public void dispose() {
            if (mCancelled.compareAndSet(false, true)) {
                if (realDispose != null) {
                    realDispose.dispose();
                    realDispose = null;
                }
            }
        }
    }
}
