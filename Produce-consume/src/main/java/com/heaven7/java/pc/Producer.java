package com.heaven7.java.pc;

import com.heaven7.java.base.util.Scheduler;

/**
 * the producer
 * @param <T> the product type.
 * @author heaven7
 */
public interface Producer<T> {

    /** flag indicate produce the product in order. */
    int FLAG_SCHEDULE_ORDERED       = 1;

    /**
     * open the producer so it can produce now. after call this {@linkplain #produce(ProductContext, Scheduler, Callback)} will be called.
     * or else. produce will always failed.
     * @return open the product factory.
     */
    boolean open();

    /**
     * start produce .
     * @param context the product context
     * @param scheduler the scheduler
     * @param callback the callback of produce.
     */
    void produce(ProductContext context, Scheduler scheduler, Callback<T> callback);

    /**
     * close the produce.
     */
    void close();

    /**
     * set the exception handle strategy.
     * @param strategy the exception strategy
     */
    void setExceptionHandleStrategy(ExceptionHandleStrategy<T> strategy);

    /**
     * add some produce flags
     * @param flags the flags to add
     */
    void addFlags(int flags);

    /**
     * has flag or not
     * @param flags the flags
     * @return true if has flag
     */
    boolean hasFlags(int flags);

    /**
     * delete the flags
     * @param flags the flags
     */
    void deleteFlags(int flags);

    /**
     * the callback of produce
     * @param <T> the product type
     */
    interface Callback<T>{
        /**
         * called on start produce
         * @param context the context
         * @param next the core produce task. never be null.
         */
        void onStart(ProductContext context, Runnable next);

        /**
         * called on produced
         * @param context the context
         * @param t the product
         * @param next the next task. never be null
         */
        void onProduced(ProductContext context, T t, Runnable next);

        /**
         * called on produce end
         * @param context the context
         */
        void onEnd(ProductContext context);
    }

    /**
     * the exception handle strategy
     * @param <T> the product type.
     */
    interface ExceptionHandleStrategy<T>{
        /**
         * called on exception occurs.
         * @param producer the producer
         * @param params the param of produce
         * @param e the exception.
         */
        void handleException(Producer<T> producer, Params params, RuntimeException e);
    }

    /**
     * the param
     */
    class Params{
        public final ProductContext context;
        public final Scheduler scheduler;
        public final ProductionFlow pf;
        public final Callback<?> callback;

        public Params(ProductContext context, Scheduler scheduler, ProductionFlow pf, Callback<?> callback) {
            this.context = context;
            this.scheduler = scheduler;
            this.pf = pf;
            this.callback = callback;
        }
    }

    /**
     * the production flow
     */
    abstract class ProductionFlow{
        public static final byte TYPE_START      = 1;
        public static final byte TYPE_END        = 2;
        public static final byte TYPE_DO_PRODUCE = 3;

        public abstract byte getType();
        public abstract Object getExtra();

        String getTypeString(){
            switch (getType()){
                case TYPE_START:
                    return "TYPE_START";
                case TYPE_END:
                    return "TYPE_END";
                case TYPE_DO_PRODUCE:
                    return "TYPE_DO_PRODUCE";
            }
            return null;
        }
    }
}
