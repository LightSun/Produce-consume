package com.heaven7.java.pc;

/**
 * the transformer
 * @param <T> the type
 * @param <R> the result type
 * @since 1.0.5
 */
public interface Transformer2<T, R> {

    /**
     * onConsume the product
     * @param context the pm context
     * @param t the product
     * @param everyParam the every parameter for every schedule
     * @return the result of onConsume
     */
    R transform(ProductContext context, T t, Object everyParam);
}
