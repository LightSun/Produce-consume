package com.heaven7.java.pc;

/**
 * the product transformer
 * @param <T> the raw product type
 * @param <R> the result product type
 * @author heaven7
 */
public interface Transformer<T,R> {

    /**
     * onConsume the product
     * @param context the pm context
     * @param t the product
     * @return the result of onConsume
     */
    R transform(ProductContext context, T t);

}
