package com.heaven7.java.pc.schedulers;

import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.base.util.threadpool.Executors2;
import com.heaven7.java.pc.internal.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author heaven7
 */
public class ComputationScheduler implements Scheduler {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final ThreadFactory sFACTORY;

    final AtomicReference<ExecutorService> mPool;

    static {
        int priority = Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY,
                Integer.getInteger(Config.KEY_COMPUTATION_PRIORITY, Thread.NORM_PRIORITY)));
        sFACTORY = new PCThreadFactory(priority, true);
    }

    public ComputationScheduler() {
        this.mPool = new AtomicReference<ExecutorService>(Executors2.newFixedThreadPool(CPU_COUNT, sFACTORY));
    }

    static int cap(int cpuCount, int paramThreads) {
        return paramThreads <= 0 || paramThreads > cpuCount ? cpuCount : paramThreads;
    }

    @Override
    public Worker newWorker() {
        return null;
    }

    private static class LoopTask implements Runnable, Disposable{
        final AtomicBoolean mCancelled = new AtomicBoolean(false);
        final List<RepeatTask> mList = new ArrayList<>();
        final BlockingQueue<RepeatTask> queue;
        final long keepTimeInMills;
        long triggerTime;
        public LoopTask(BlockingQueue<RepeatTask> queue, long keepTimeInMills) {
            this.queue = queue;
            this.keepTimeInMills = keepTimeInMills;
        }
        public void schedule(Runnable task, long initDelay, long period, TimeUnit unit){
            queue.offer(new RepeatTask(task, unit.toMillis(initDelay), unit.toMillis(period), -1));
        }
        @Override
        public void run() {
            triggerTime = System.currentTimeMillis();
            while (shouldCancel()){
                long time = System.currentTimeMillis();
                int count = queue.drainTo(mList);
                for (int i = 0 ; i < count ; i ++){
                    RepeatTask task = mList.get(i);
                    if(!task.run(time)){
                        queue.remove(task);
                    }
                }
            }
        }
        boolean shouldCancel(){
            long curTime = System.currentTimeMillis();
            if(keepTimeInMills > 0){
                if((curTime - triggerTime) >= keepTimeInMills){
                    return true;
                }
            }
            return mCancelled.get();
        }
        @Override
        public void dispose() {
            mCancelled.compareAndSet(false, true);
        }
    }

    private static class RepeatTask implements Disposable{
        final AtomicBoolean mCancelled = new AtomicBoolean(false);
        private final Runnable mTask;
        private final int mMaxCount;

        final long triggerTime;
        final long period;
        final long startDelay;

        int mRunCount;
        long lastRunTime;

        public RepeatTask(Runnable mTask,long startDelay, long period,int mMaxCount) {
            this.mTask = mTask;
            this.mMaxCount = mMaxCount;
            this.period = period;
            this.startDelay = startDelay;
            this.triggerTime = System.currentTimeMillis();
        }

        public boolean run(long time){
            if(mCancelled.get()){
                return false;
            }
            final int runCount = this.mRunCount;
            if(mMaxCount > 0 && runCount >= mMaxCount){
               return false;
            }
            if(runCount == 0){
               //first time
                if((time - triggerTime) >= startDelay){
                    mTask.run();
                    mRunCount ++;
                    lastRunTime = triggerTime + startDelay;
                }
            }else {
                if((time - lastRunTime) >= period ){
                    mTask.run();
                    mRunCount ++;
                    lastRunTime += period;
                }
            }
            return true;
        }

        @Override
        public void dispose() {
            mCancelled.compareAndSet(false, true);
        }
    }
}
