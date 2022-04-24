package com.heaven7.java.pc.producers;

import com.heaven7.java.base.util.Predicates;
import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.base.util.ThreadProxy;
import com.heaven7.java.pc.BaseProducer;
import com.heaven7.java.pc.ProductContext;
import com.heaven7.java.pc.TaskNode;
import com.heaven7.java.pc.internal.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * create a pipe producer , who used a absolute thread to wait msg which was produced by {@linkplain Pipe#addProduct(Object)}
 * or {@linkplain Pipe#addProducts(List)}.
 *
 * @author heaven7
 */
public final class PipeProducer<T> extends BaseProducer<T> implements Runnable {

    /**
     * the pipe of producer to produce products
     * @param <T> the type of products
     */
    public interface Pipe<T> {

        /**
         * add a product to the producer
         * @param t the product
         */
        void addProduct(T t);
        /**
         * add  products to the producer
         * @param ts the products
         */
        void addProducts(List<T> ts);

        /**
         * close the pipe. this means it will make producer perform a end cmd.
         * that means the producer will be closed after tasks done.
         */
        void close();

        /**
         * indicate the pipe is closed or not. unless you call {@linkplain #close()} or else this always return false.
         * @return true if pip is closed.
         */
        boolean isClosed();
    }

    private static final ThreadFactory sFactory =
            new ThreadFactory() {
                final AtomicInteger sIndex = new AtomicInteger();

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setPriority(Thread.NORM_PRIORITY);
                    thread.setName("PipeProducer__" + sIndex.incrementAndGet());
                    return thread;
                }
            };
    private final Object mMonitor = new Object();
    private final Pipe0<T> mPipe = new Pipe0<T>(this);
    private final ThreadProxy mThreadProxy = ThreadProxy.create(sFactory);
    private final AtomicBoolean mLastDone = new AtomicBoolean(true);
    private final BlockingDeque<Msg> mQueue;
    private boolean mOrdered;

    private ProductContext mContext;
    private Scheduler mScheduler;
    private Callback<T> mCallback;
    private final Runnable mEndRun =
            new Runnable() {
                @Override
                public void run() {
                    mLastDone.compareAndSet(false, true);
                    firePending();
                }
            };

    public PipeProducer() {
        this(Integer.MAX_VALUE);
    }

    public PipeProducer(int queueSize) {
        mQueue = new LinkedBlockingDeque<>(queueSize);
    }

    /**
     * get the left product size.
     * @return the product size.
     * @since 1.0.2
     */
    public int getLeftProductSize(){
        return mQueue.size() + mPipe.getPendingSize();
    }

    @Override
    protected void produceOrdered(
            ProductContext context, Scheduler scheduler, Callback<T> callback) {
        setInternal(context, scheduler, callback);
        mOrdered = true;
        startInternal();
    }

    @Override
    protected void produce0(ProductContext context, Scheduler scheduler, Callback<T> callback) {
        setInternal(context, scheduler, callback);
        mOrdered = false;
        startInternal();
    }

    @Override
    public void close() {
        mThreadProxy.dispose();
        super.close();
        reset();
    }

    public Pipe<T> getPipe() {
        return mPipe;
    }

    private void startInternal() {
        mThreadProxy.start(this);
        firePending();
    }

    private void firePending() {
        // fire products
        if (mPipe.hasPendingProducts()) {
            ArrayList<T> list = new ArrayList<>();
            mPipe.drainTo(list);
            fire(null, list);
        }
        // fire end
        if (mPipe.mCloseCmdActivated.compareAndSet(true, false)) {
            fire(null, null);
        }
        synchronized (mMonitor) {
            mMonitor.notify();
        }
    }

    private void setInternal(ProductContext context, Scheduler scheduler, Callback<T> callback) {
        this.mContext = context;
        this.mScheduler = scheduler;
        this.mCallback = callback;
    }

    boolean isPrepared() {
        return mCallback != null && mLastDone.get();
    }

    private void fire(T t, List<T> list) {
        // System.out.println("t = " + t + " ,list = " + (list != null ? list.size() : 0));
        mQueue.addLast(new Msg(t, list));
    }

    private void markProduceEnd() {
        if (!mOrdered) {
            markProduceEnd(mContext, mScheduler, mCallback);
        } else {
            endImpl(mContext, mScheduler, mCallback);
        }
    }

    private void reset() {
        mQueue.clear();
        mLastDone.compareAndSet(false, true);
        setInternal(null, null, null);
        mPipe.reset();
    }

    @Override
    public void run() {
        try {
            while (isOpened()) {
                Msg msg = mQueue.take();
                // System.out.println("msg.hasProduct = " + msg.hasProduct());
                if (!msg.hasProduct()) {
                    if (mLastDone.get()) {
                        markProduceEnd();
                    } else {
                        mQueue.addLast(msg);
                        synchronized (mMonitor) {
                            mMonitor.wait();
                        }
                    }
                } else {
                    if (mOrdered) {
                        if (mLastDone.compareAndSet(true, false)) {
                            if (msg.product != null) {
                                scheduleOrdered(
                                        mContext, mScheduler, msg.product, mCallback, mEndRun);
                            } else {
                                TaskNode<T> node =
                                        Utils.generateOrderedTasks(
                                                this,
                                                msg.products,
                                                mContext,
                                                mScheduler,
                                                mCallback);
                                node.setTailNext(mEndRun);
                                node.scheduleOrdered();
                            }
                        } else {
                            // last task not done. wait.
                            mQueue.addFirst(msg);
                            synchronized (mMonitor) {
                                mMonitor.wait();
                            }
                        }
                    } else {
                        if (msg.product != null) {
                            scheduleImpl(mContext, mScheduler, msg.product, mCallback);
                        } else {
                            for (T product : msg.products) {
                                scheduleImpl(mContext, mScheduler, product, mCallback);
                            }
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private class Msg {
        final List<T> products;
        final T product;

        Msg(T product, List<T> products) {
            this.product = product;
            this.products = products;
        }

        boolean hasProduct() {
            return product != null || !Predicates.isEmpty(products);
        }
    }

    private static class Pipe0<T> implements Pipe<T> {

        final PipeProducer<T> producer;
        final List<T> pendings = new ArrayList<>();
        final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
        /** the flag to act close when producer is ready */
        final AtomicBoolean mCloseCmdActivated = new AtomicBoolean(false);
        final AtomicBoolean mPipClosed = new AtomicBoolean(false);

        public Pipe0(PipeProducer<T> producer) {
            this.producer = producer;
        }

        @Override
        public void addProduct(T t) {
            if(isClosed()){
                System.err.println("pip is closed");
                return;
            }
            if (producer.isPrepared()) {
                producer.fire(t, null);
            } else {
                lock.writeLock().lock();
                try {
                    pendings.add(t);
                } finally {
                    lock.writeLock().unlock();
                }
            }
        }

        @Override
        public void addProducts(List<T> ts) {
            if(isClosed()){
                System.err.println("pip is closed");
                return;
            }
            if (producer.isPrepared()) {
                producer.fire(null, ts);
            } else {
                lock.writeLock().lock();
                try {
                    pendings.addAll(ts);
                } finally {
                    lock.writeLock().unlock();
                }
            }
        }

        @Override
        public void close() {
            if(mPipClosed.compareAndSet(false, true)){
                if (producer.isPrepared()) {
                    producer.fire(null, null);
                } else {
                    mCloseCmdActivated.compareAndSet(false, true);
                }
            }
        }

        @Override
        public boolean isClosed() {
            return mPipClosed.get();
        }

        public boolean hasPendingProducts() {
            lock.readLock().lock();
            try {
                return pendings.size() > 0;
            } finally {
                lock.readLock().unlock();
            }
        }

        public void drainTo(List<T> list) {
            lock.writeLock().lock();
            try {
                list.addAll(pendings);
                pendings.clear();
            } finally {
                lock.writeLock().unlock();
            }
        }

        public void reset() {
            mCloseCmdActivated.compareAndSet(true, false);
            mPipClosed.compareAndSet(true, false);
            lock.writeLock().lock();
            try {
                pendings.clear();
            } finally {
                lock.writeLock().unlock();
            }
        }
        public int getPendingSize() {
            lock.readLock().lock();
            try {
                return pendings.size();
            }finally {
                lock.readLock().unlock();
            }
        }
    }
}
