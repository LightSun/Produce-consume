package com.heaven7.java.pc;

import com.heaven7.java.base.util.Scheduler;

/**
 * the task node which indicate a list of tasks.
 * @param <T> the product type.
 */
public class TaskNode<T> implements Runnable {

    public static final TaskNode<?> EMPTY = new TaskNode<>(null, null, null, null);

    private final BaseProducer<T> producer;
    private final ProductContext context;
    private final Scheduler scheduler;
    private final Producer.Callback<T> callback;

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
            producer.scheduleOrdered(context, scheduler, current, callback,
                        nextTask != null ? this : null);
        }
    }

    public void reset(){
        current = null;
        if(nextTask != null){
            nextTask.reset();
            nextTask = null;
        }
    }

    @Override
    public void run() {
        if (nextTask != null) {
            nextTask.scheduleOrdered();
            nextTask = null;
        }
    }
}