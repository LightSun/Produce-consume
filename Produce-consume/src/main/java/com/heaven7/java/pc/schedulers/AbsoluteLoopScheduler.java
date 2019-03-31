package com.heaven7.java.pc.schedulers;

import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.pc.internal.Config;
import com.heaven7.java.pc.internal.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author heaven7
 */
public class AbsoluteLoopScheduler extends BaseExecutorServiceScheduler implements Scheduler, Runnable{

   // private static final float SLEEP_PERCENT;
    private static final long LOOP_KEEP_TIME;
    private static final int QUEUE_SIZE;

    static {
      /*  Float percent = Utils.getFloat(Config.KEY_LOOP_SLEEP_PERCENT);
        SLEEP_PERCENT = percent != null ? Math.min(percent, 0.8f) : 0.5f;*/
        Long time = Long.getLong(Config.KEY_ABSOLUTE_LOOP_KEEP_TIME);
        LOOP_KEEP_TIME = time != null ? time : -1;
        Integer size = Integer.getInteger(Config.KEY_ABSOLUTE_QUEUE_SIZE);
        QUEUE_SIZE = size != null ? size : 128;
    }

    private volatile Thread mThread;
    private AtomicReference<LoopTask> mLoopTaskRef = new AtomicReference<>(null);

    public AbsoluteLoopScheduler(ExecutorService mService) {
        super(mService);
        startLoop();
    }

    private void startLoop() {
        if(mThread == null){
            LoopTask loopTask = new LoopTask(new ArrayBlockingQueue<RepeatTask>(QUEUE_SIZE), LOOP_KEEP_TIME, this);
            if(mLoopTaskRef.compareAndSet(null, loopTask)){
                mThread = new Thread(loopTask);
                mThread.start();
            }
        }
    }
    private void destroyLoop(){
        if(mThread != null) {
            mThread.interrupt();
            mThread = null;
            mLoopTaskRef.getAndSet(null);
        }
    }

    @Override
    public Worker newWorker() {
        startLoop();
        return new CommonWorker(getExecutorService(), mLoopTaskRef.get());
    }

    @Override
    public void run() {
        destroyLoop();
    }

    private static class CommonWorker implements Worker{

        private final ExecutorService mService;
        private final LoopTask mLoopTask;

        public CommonWorker(ExecutorService mService, LoopTask mLoopTask) {
            this.mService = mService;
            this.mLoopTask = mLoopTask;
        }
        @Override
        public Disposable schedule(Runnable task) {
            return new Disposable.FutureDisposable(mService.submit(task));
        }
        @Override
        public Disposable scheduleDelay(Runnable task, long delay, TimeUnit unit) {
            return mLoopTask.schedule(task, delay, -1, unit,1);
        }
        @Override
        public Disposable schedulePeriodically(Runnable task, long initDelay, long period, TimeUnit unit) {
            return mLoopTask.schedule(task, initDelay, period, unit);
        }
    }

    private static class LoopTask implements Runnable, Disposable{
        final AtomicBoolean mCancelled = new AtomicBoolean(false);
        final List<RepeatTask> mList = new ArrayList<>();
        final BlockingQueue<RepeatTask> queue;

       // final AtomicLong minTime = new AtomicLong(-1);
        final Runnable keepTimeEnd;
        final long keepTimeInMills;
        long triggerTime;

        public LoopTask(BlockingQueue<RepeatTask> queue, long keepTimeInMills, Runnable keepTimeEnd) {
            this.queue = queue;
            this.keepTimeEnd = keepTimeEnd;
            this.keepTimeInMills = keepTimeInMills;
        }
        public Disposable schedule(Runnable task, long initDelay, long period, TimeUnit unit){
            return schedule(task, initDelay, period, unit, -1);
        }
        public Disposable schedule(Runnable task, long initDelay, long period, TimeUnit unit, int maxCount){
            RepeatTask task1 = new RepeatTask(task, unit.toMillis(initDelay), unit.toMillis(period), maxCount);
            queue.offer(task1);
            synchronized (this){
                this.notify();
            }
            //compute sleep time
          /*  long old = minTime.get();
            final long min = old > 0 ? Math.min(unit.toMillis(period), old) : unit.toMillis(period);
            minTime.updateAndGet(new LongUnaryOperator() {
                @Override
                public long applyAsLong(long operand) {
                    return min;
                }
            });*/
            return task1;
        }
        @Override
        public void run() {
            triggerTime = System.currentTimeMillis();
            try {
                while (!shouldCancel()){
                    final int count = queue.drainTo(mList);
                    if(count == 0){
                        synchronized (this){
                            this.wait();
                        }
                        continue;
                    }
                    for (int i = 0 ; i < count ; i ++){
                        RepeatTask task = mList.get(i);
                        if(!task.run()){
                            queue.remove(task);
                        }
                    }
                    mList.clear();
                }
                if(keepTimeEnd != null){
                    keepTimeEnd.run();
                }
            }catch (InterruptedException e){
                //ignore
                System.out.println("LoopTask is interrupted now .");
            }
        }
        boolean shouldCancel(){
            if(keepTimeInMills > 0){
                if((System.currentTimeMillis() - triggerTime) >= keepTimeInMills){
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
        int getRunCount(){
            return mRunCount;
        }

        public boolean run(){
            if(mCancelled.get()){
                return false;
            }
            final int runCount = this.mRunCount;
            if(mMaxCount > 0 && runCount >= mMaxCount){
                return false;
            }
            long time = Utils.currentTimeMillis();
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
