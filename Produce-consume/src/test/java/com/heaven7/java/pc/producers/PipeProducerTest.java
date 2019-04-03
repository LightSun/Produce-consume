package com.heaven7.java.pc.producers;

import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.pc.Producer;
import com.heaven7.java.pc.schedulers.Schedulers;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author heaven7
 */
public class PipeProducerTest extends IterableProducerTest {

    private final Scheduler mScheduler = Schedulers.single();

    @Override
    protected <T> Producer<T> createProducer(Collection<T> tasks) {
        PipeProducer<T> producer = new PipeProducer<>(tasks.size());
        mScheduler.newWorker().schedule(new Task<T>(tasks, producer.getPipe()));
        return producer;
    }

    private static class Task<T> implements Runnable{
        final Collection<T> tasks;
        final PipeProducer.Pipe<T> pipe;

        public Task(Collection<T> tasks, PipeProducer.Pipe<T> pipe) {
            this.tasks = tasks;
            this.pipe = pipe;
        }
        @Override
        public void run() {
            //here mock the task
            pipe.addProducts(new ArrayList<T>(tasks));
            pipe.close();
           // markFinished();
        }
    }
}
