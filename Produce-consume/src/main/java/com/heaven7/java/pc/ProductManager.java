package com.heaven7.java.pc;

import com.heaven7.java.base.util.Scheduler;

/**
 * @author heaven7
 */
public interface ProductManager<T, R> {

    void setProductContext(ProductContext context);
    ProductContext getProductContext();

    void setScheduler(Scheduler scheduler);
    Scheduler getScheduler();

    void setTransformer(Transformer<? super T, R> transformer);
    Transformer<? super T, R> getTransformer();

    /**
     * open the pm with transformer and consumers
     * @param collector the consumers which used to consumer result.
     * @return true if open success.
     */
    boolean open(Consumer<? super R> collector);

    /**
     * close the pm. this make the consumers end.
     */
    void close();
}
