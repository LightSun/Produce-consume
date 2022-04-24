package com.heaven7.java.pc;

/**
 * the wrapped product context
 * @author heaven7
 */
public class WrappedProductContext implements ProductContext {

    private final ProductContext base;

    public WrappedProductContext(ProductContext base) {
        this.base = base;
    }
    public ProductContext getBase() {
        return base;
    }
}
