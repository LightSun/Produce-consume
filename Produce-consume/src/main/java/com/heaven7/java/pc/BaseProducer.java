package com.heaven7.java.pc;

import com.heaven7.java.base.anno.JustForTest;
import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.base.util.Throwables;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * the producer which can schedule the tasks in order or not.
 * @author heaven7
 */
public abstract class BaseProducer<T> implements Producer<T>, CancelableTask.Callback {

    public static final Runnable EMPTY_TASK = new Runnable() {
        @Override
        public void run() {

        }
    };
    private static final int STATE_START      = 1;
    private static final int STATE_END        = 2;
    private static final int STATE_NONE       = 0;

    private final AtomicBoolean mClosed = new AtomicBoolean(true);
    private final Set<CancelableTask> mTasks = Collections.synchronizedSet(new HashSet<CancelableTask>());
    private final AtomicInteger mProduceState = new AtomicInteger(STATE_NONE);
    private ExceptionHandleStrategy<T> mExceptionStrategy;
    private int mFlags;

    protected static class SimpleProductionFlow extends ProductionFlow{

        private final byte type;
        private final Object extra;
        public SimpleProductionFlow(byte type, Object extra) {
            this.type = type;
            this.extra = extra;
        }
        @Override
        public byte getType() {
            return type;
        }
        @Override
        public Object getExtra() {
            return extra;
        }
    }

    @Override
    public void addFlags(int flags) {
        this.mFlags |= flags;
    }

    @Override
    public boolean hasFlags(int flags) {
        return (this.mFlags & flags) == flags;
    }

    @Override
    public void deleteFlags(int flags) {
        this.mFlags &= ~flags;
    }

    @Override
    public void setExceptionHandleStrategy(ExceptionHandleStrategy<T> strategy) {
        this.mExceptionStrategy = strategy;
    }

    public TaskNode<T> createTaskNode(ProductContext context, Scheduler scheduler, Callback<T> callback){
         return new TaskNode<T>(this, context, scheduler, callback);
    }

    @Override
    public boolean isOpened() {
        return !mClosed.get();
    }
    @Override
    public boolean open() {
        return mClosed.compareAndSet(true, false);
    }

    public boolean isClosed(){
        return mClosed.get();
    }
    @Override
    public void close() {
        if(mClosed.compareAndSet(false, true)){
            for (CancelableTask task : mTasks){
                task.cancel();
                task.reset();
            }
            mTasks.clear();
        }
    }

    @Override
    public final void produce(final ProductContext context, final Scheduler scheduler, final Callback<T> callback) {
        mProduceState.compareAndSet(STATE_NONE, STATE_START);
        final boolean ordered = hasFlags(FLAG_SCHEDULE_ORDERED);
        final Runnable produce = new Runnable() {
            @Override
            public void run() {
                if(ordered){
                    produceOrdered(context, scheduler, callback);
                }else {
                    produce0(context, scheduler, callback);
                }
            }
        };
        post(scheduler, new Runnable() {
            @Override
            public void run() {
                callback.onStart(context, produce);
            }
        }, new Params(context, scheduler, new SimpleProductionFlow(ProductionFlow.TYPE_START, null), callback));
    }

    /**
     * schedule the task , but may not in order.
     * @param context the context
     * @param scheduler the scheduler
     * @param t the product
     * @param callback the callback
     * @return the the scheduled task. which can be cancelled.
     */
    public final Disposable scheduleImpl(final ProductContext context, final Scheduler scheduler, final T t,
                                             final Callback<T> callback){
        return post(scheduler, new Runnable() {
            @Override
            public void run() {
                if(!isClosed()){
                    callback.onProduced(context, t, EMPTY_TASK);
                    //if is closed or end. dispatch end. left 1 .means only current task is running.
                    checkEndState(context, scheduler, callback, 1);
                }else {
                    endImpl(context, scheduler, callback);
                }
            }
        }, new Params(context, scheduler, new SimpleProductionFlow(ProductionFlow.TYPE_DO_PRODUCE, t), callback));
    }

    /**
     * schedule the task as ordered. if producer is closed or next is null. will dispatch end.
     * @param context the context
     * @param scheduler the scheduler
     * @param t the product to schedule
     * @param callback the callback
     * @param next the next. if is null. means will end.
     * @return the the scheduled task. which can be cancelled.
     */
    public final Disposable scheduleOrdered(final ProductContext context, final Scheduler scheduler, final T t,
                                                final Callback<T> callback, final Runnable next){
        final Runnable nextRun = new Runnable() {
            @Override
            public void run() {
                //if is closed or end. dispatch end
                if(isClosed() || next == null){
                    if(next != null && next instanceof TaskNode){
                        ((TaskNode) next).reset();
                    }
                    endImpl(context, scheduler, callback);
                }else {
                    next.run();
                }
            }
        };
        return post(scheduler, new Runnable() {
            @Override
            public void run() {
                callback.onProduced(context, t, nextRun);
            }
        }, new Params(context, scheduler, new SimpleProductionFlow(ProductionFlow.TYPE_DO_PRODUCE, t), callback));
    }

    protected Disposable post(Scheduler scheduler, Runnable task, Params params){
        CancelableTask cancelableTask = CancelableTask.of(task, this);
        cancelableTask.setProduceParams(params);
        Disposable disposable = scheduler.newWorker().schedule(cancelableTask.toActuallyTask());
        cancelableTask.setDisposable(disposable);
        return cancelableTask;
    }

    protected Disposable postDelay(Scheduler scheduler, Runnable task, long delayInMills, Params params){
        CancelableTask cancelableTask = CancelableTask.of(task, this);
        cancelableTask.setProduceParams(params);
        Disposable disposable = scheduler.newWorker().scheduleDelay(
                cancelableTask.toActuallyTask(), delayInMills, TimeUnit.MILLISECONDS);
        cancelableTask.setDisposable(disposable);
        return cancelableTask;
    }

    @JustForTest
    public void assertTaskIsEmpty(){
        Throwables.checkArgument(mTasks.isEmpty(), "task is not empty.");
    }

    private void endImpl(final ProductContext context, Scheduler scheduler, final Callback<T> callback) {
        post(scheduler, new Runnable() {
            @Override
            public void run() {
                close();
                mProduceState.compareAndSet(STATE_END, STATE_NONE);
                callback.onEnd(context);
            }
        }, new Params(context, scheduler, new SimpleProductionFlow(ProductionFlow.TYPE_END, null),
                callback));
    }
    @Override
    public void onTaskPlan(CancelableTask wrapTask) {
         mTasks.add(wrapTask);
    }

    @Override
    public void onTaskBegin(CancelableTask wrapTask) {

    }
    @Override
    public void onTaskEnd(CancelableTask wrapTask, boolean cancelled) {
        mTasks.remove(wrapTask);
    }
    @Override
    public void onException(CancelableTask wrapTask, RuntimeException e) {
        Params params = wrapTask.getProduceParams();
        mTasks.remove(wrapTask);
        if(mExceptionStrategy != null){
            mExceptionStrategy.handleException(this, params, e);
        }else {
            throw e;
        }
    }

    /**
     * check the end state. if the end state and no more tasks. just dispatch end.
     * @param context the context
     * @param scheduler the scheduler.
     * @param callback the callback
     * @param leftTaskCount the left task count. for {@linkplain #markProduceEnd(ProductContext, Scheduler, Callback)} this is 0.
     */
    private void checkEndState(ProductContext context, Scheduler scheduler, Callback<T> callback, int leftTaskCount) {
        //this method help check end state for produce no-order products.
        if(mProduceState.get() == STATE_END && mTasks.size() == leftTaskCount){
            endImpl(context, scheduler, callback);
        }
    }

    /**
     * mark produce end. this often called for produce on 'no-order'.
     * @param context the product context
     * @param scheduler the scheduler
     * @param callback the callback
     */
    protected void markProduceEnd(ProductContext context, Scheduler scheduler, Callback<T> callback){
        if(!isClosed()){
            if(mProduceState.compareAndSet(STATE_START, STATE_END)){
                checkEndState(context, scheduler, callback, 0);
            }
        }
    }

    /**
     * call this to produce all products really. note: this produce may not in order. it is determined by scheduler.
     * @param context the product context
     * @param scheduler the scheduler. can be null
     * @param callback the callback
     */
    protected void produce0(ProductContext context, Scheduler scheduler, Callback<T> callback){

    }
    /**
     * produce the product in ordered.
     * @param context the context
     * @param scheduler the scheduler
     * @param callback the callback
     */
    protected abstract void produceOrdered(ProductContext context, Scheduler scheduler, Callback<T> callback);
}
