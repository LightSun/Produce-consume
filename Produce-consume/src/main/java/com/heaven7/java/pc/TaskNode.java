package com.heaven7.java.pc;

import com.heaven7.java.base.util.Scheduler;

/**
 * the task node which indicate an ordered  list tasks.
 * @param <T> the product type.
 */
public final class TaskNode<T> implements Runnable {

    public static final TaskNode<?> EMPTY = new TaskNode<>(null, null, null, null);

    private final BaseProducer<T> producer;
    private final ProductContext context;
    private final Scheduler scheduler;
    private final Producer.Callback<T> callback;
    private Runnable next;

    public T current;
    public TaskNode nextTask;

    public TaskNode(BaseProducer<T> producer, ProductContext context, Scheduler scheduler, Producer.Callback<T> callback) {
        this.producer = producer;
        this.context = context;
        this.scheduler = scheduler;
        this.callback = callback;
    }

    public void scheduleOrdered() {
        if (current != null) {
            Runnable toNext = nextTask != null ? this : next;
            producer.scheduleOrdered(context, scheduler, current, callback, toNext);
        }
    }

    /**
     * set the runnable attach to the tail node. as the final runnable.
     * @param next the final runnable
     */
    public void setTailNext(Runnable next) {
        getTailNode().next = next;
    }

    public void reset(){
        current = null;
        if(nextTask != null){
            nextTask.reset();
            nextTask = null;
        }
    }

    private TaskNode getTailNode(){
        if(nextTask == null){
            return this;
        }
        return nextTask.getTailNode();
    }

    @Override
    public void run() {
        if (nextTask != null) {
            nextTask.scheduleOrdered();
            nextTask = null;
        }
    }
}