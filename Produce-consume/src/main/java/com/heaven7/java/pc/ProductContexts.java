package com.heaven7.java.pc;

public final class ProductContexts {

    public static SimpleProductContext of(Object data){
        return new SimpleProductContext(data);
    }
}

