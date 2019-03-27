package com.heaven7.java.pc.pm;

import com.heaven7.java.pc.Producer;
import com.heaven7.java.pc.Transformers;

/**
 * @author heaven7
 */
public class DirectProductManager<T> extends SimpleProductManager<T,T> {

    public DirectProductManager(Producer<T> producer) {
        super(producer);
        setTransformer(Transformers.<T>unchangeTransformer());
    }
}
