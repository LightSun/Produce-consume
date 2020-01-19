package com.heaven7.java.pc;

import com.heaven7.java.base.util.Scheduler;

import java.util.Collection;

/**
 * the product manager
 * @param <T> the raw product type
 * @param <R> the result product type after transform.
 * @author heaven7
 */
public interface ProductManager<T, R> {

    /**
     * set the product context
     * @param context the product context
     */
    void setProductContext(ProductContext context);

    /**
     * the product context
     * @return the product context
     */
    ProductContext getProductContext();

    /**
     * set the scheduler
     * @param scheduler the scheduler
     */
    void setScheduler(Scheduler scheduler);

    /**
     * get the scheduler
     * @return the scheduler
     */
    Scheduler getScheduler();

    /**
     * set product transformer
     * @param transformer the transformer
     */
    void setTransformer(Transformer<? super T, R> transformer);

    /**
     * get the product transformer
     * @return true.
     */
    Transformer<? super T, R> getTransformer();

    /**
     * indicate the product manager is opened or not.
     * @return true if it is opened
     */
    boolean isOpened();
    /**
     * open the pm with transformer and consumers
     * @param collector the consumers which used to consumer result.
     * @return true if open success. false if already opened.
     */
    boolean open(Consumer<? super R> collector);


    /**
     * open the producer with multi consumers. that means one product will only dispatch to one consumer.
     * @param candidateConsumers the Candidate consumers
     * @param selectionStrategy the select strategy
     * @return  true if open success, false if already opened.
     */
    boolean openMulti(Collection<Consumer<? super R>> candidateConsumers, SelectionStrategy<R> selectionStrategy);

    /**
     * close the pm. this make the consumers end.
     */
    void close();
}
