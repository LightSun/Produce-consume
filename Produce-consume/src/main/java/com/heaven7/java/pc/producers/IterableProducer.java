package com.heaven7.java.pc.producers;

import com.heaven7.java.base.anno.Nullable;
import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.pc.Producer;
import com.heaven7.java.pc.ProductContext;
import com.heaven7.java.pc.TaskNode;
import com.heaven7.java.pc.internal.Utils;

import java.util.Iterator;

/**
 * produce it as Iterator.
 * @author heaven7
 */
public class IterableProducer<T> extends BaseProducer<T> implements Producer<T> {

    private final Iterable<T> it;

    public IterableProducer(Iterable<T> it) {
        this.it = it;
    }

    @Override
    protected void produce0(final ProductContext context, @Nullable Scheduler scheduler, final Callback<T> callback) {
        Iterator<T> it = this.it.iterator();
        while (it.hasNext() && !isClosed()){
            scheduleImpl(context, scheduler, it.next(), callback, !it.hasNext());
        }
    }

    @Override
    protected void produceOrdered(ProductContext context, Scheduler scheduler, Callback<T> callback) {
        TaskNode node = Utils.generateOrderedTasks(this, it, context, scheduler, callback);
        node.scheduleOrdered();
    }
}
