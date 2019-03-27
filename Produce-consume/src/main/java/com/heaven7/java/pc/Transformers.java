package com.heaven7.java.pc;

/**
 * @author heaven7
 */
public class Transformers {

    private static final Transformer<Object, Object> UNCHANGED = new Transformer<Object, Object>() {
        @Override
        public Object consume(ProductContext context, Object t) {
            return t;
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> Transformer<T,T> unchangeTransformer(){
        return (Transformer<T, T>) UNCHANGED;
    }
}
