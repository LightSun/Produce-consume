package com.heaven7.java.pc.async;

public final class AsyncException extends RuntimeException{

    public AsyncException() {
    }

    public AsyncException(String message) {
        super(message);
    }

    public AsyncException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsyncException(Throwable cause) {
        super(cause);
    }
}
