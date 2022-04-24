package com.heaven7.java.pc.async;
/**
 * @author heaven7
 * @since 1.0.5
 */
public final class Response<T> {

    public static final int STATE_EXCEPTION = -1;
    public static final int STATE_EMPTY     = -2;
    public static final int STATE_NET_ERROR = -3;
    public static final int STATE_SUCCESS   = 0;

    private int state;
    private T data;
    private Throwable throwable;

    public Response(){}

    public Response(int state, T data, Throwable throwable) {
        this.state = state;
        this.data = data;
        this.throwable = throwable;
    }

    public static <T> Response<T> ofSuccess(T data){
        return new Response<>(STATE_SUCCESS, data, null);
    }
    @SuppressWarnings("unchecked")
    public static Response ofThrowable(Throwable e){
        return new Response(STATE_EXCEPTION, null, e);
    }
    @SuppressWarnings("unchecked")
    public static Response<?> ofEmpty(){
        return new Response(STATE_EMPTY, null, null);
    }

    @Override
    public String toString() {
        return "Response{" +
                "state=" + state +
                ", data=" + data +
                ", throwable=" + throwable +
                '}';
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
