package com.heaven7.java.pc.internal;

/**
 * @author heaven7
 */
public final class Exceptions {

    public static RuntimeException cast(Throwable e){
        //TODO latter will changed
        return e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
    }
}
