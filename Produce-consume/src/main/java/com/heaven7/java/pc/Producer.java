package com.heaven7.java.pc;

import com.heaven7.java.base.util.Scheduler;

/**
 * @author heaven7
 */
public interface Producer<T> {

    int FLAG_SCHEDULE_ORDERED       = 1;

    boolean open();

    void produce(ProductContext context, Scheduler scheduler, Callback<T> callback);

    void close();

    void setExceptionHandleStrategy(ExceptionHandleStrategy<T> strategy);

    void addFlags(int flags);
    boolean hasFlags(int flags);
    void deleteFlags(int flags);

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
    interface ExceptionHandleStrategy<T>{
        void handleException(Producer<T> producer, Params params, RuntimeException e);
    }

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
