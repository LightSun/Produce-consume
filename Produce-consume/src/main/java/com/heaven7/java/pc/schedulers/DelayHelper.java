package com.heaven7.java.pc.schedulers;

import com.heaven7.java.base.util.DefaultPrinter;
import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.pc.internal.Config;
import com.heaven7.java.pc.internal.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author heaven7
 */
public final class DelayHelper implements DelayTaskLooper, Runnable, Disposable {

    private static final int QUEUE_SIZE;
    static {
        Integer size = Integer.getInteger(Config.KEY_DELAY_QUEUE_SIZE);
        QUEUE_SIZE = size != null ? size : 128;
    }
    private static final String TAG = "DelayHelper";

    private final AtomicBoolean mCancelled = new AtomicBoolean(false);
    private final BlockingQueue<Task> mQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
    private final List<Task> mList = new ArrayList<>();
    private final Thread mThread;

    public DelayHelper() {
        this.mThread = new Thread(this);
       // this.mThread.setDaemon(true);
        this.mThread.start();
    }

    @Override
    public Disposable scheduleDelay(Executor executor, Runnable task, long delay) {
        Task realTask = new Task(task, delay, executor);
        mQueue.offer(realTask);
        synchronized (this){
            this.notify();
        }
        return realTask;
    }

    @Override
    public void run() {
        try {
            while (!mCancelled.get()) {
                int count = mQueue.drainTo(mList);
                if (count == 0) {
                    synchronized (this) {
                        this.wait();
                    }
                    continue;
                }
                for (int i = 0 ; i < count ; i ++){
                    Task task = mList.get(i);
                    if(!task.run()){
                        mQueue.remove(task);
                    }
                }
                mList.clear();
            }
        } catch (InterruptedException e) {
           // e.printStackTrace();
        }
    }
    @Override
    public void dispose() {
        if(mCancelled.compareAndSet(false, true)){
            mThread.interrupt();
        }
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
            if(Utils.currentTimeMillis() < targetTime){
                return true;
            }
           return smartExecutor.execute(task);
        }
        @Override
        public void dispose() {
            smartExecutor.dispose();
        }
    }

    private static class SmartExecutor implements Disposable{
        final AtomicBoolean mCancelled = new AtomicBoolean(false);
        private Executor executor;
        private WeakReference<ExecutorService> weakExecutor;
        Disposable realDispose;

        public SmartExecutor(Executor executor) {
            if(executor instanceof ExecutorService){
                weakExecutor = new WeakReference<ExecutorService>((ExecutorService) executor);
            }else {
                this.executor = executor;
            }
        }
        //return false. means task will be remove.
        public boolean execute(final Runnable task){
            if(mCancelled.get()){
                return false;
            }
            if(executor == null){
                ExecutorService service = weakExecutor.get();
                if(service != null){
                    realDispose = new FutureDisposable(service.submit(task));
                }else {
                    DefaultPrinter.getDefault().error(TAG,"execute","scheduler does not exist .");
                }
            }else {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if(!mCancelled.get()){
                            task.run();
                        }
                    }
                });
            }
            return false;
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
