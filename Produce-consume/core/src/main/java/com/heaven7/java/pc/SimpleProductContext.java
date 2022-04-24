package com.heaven7.java.pc;

/**
 * @since 1.0.5
 */
public class SimpleProductContext implements ProductContext{

    private final Object data;

    public SimpleProductContext(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }
}
