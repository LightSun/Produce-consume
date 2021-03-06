package com.heaven7.java.pc;

/**
 * @author heaven7
 */
public final class Transformers {

    private static final Transformer<Object, Object> UNCHANGED = new Transformer<Object, Object>() {
        @Override
        public Object transform(ProductContext context, Object t) {
            return t;
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> Transformer<T,T> unchangeTransformer(){
        return (Transformer<T, T>) UNCHANGED;
    }
}
