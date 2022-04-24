package com.heaven7.java.pc.async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @since 1.0.5
 */
public final class Asyncs {

    public static <In, Out> Async<In, Out> just(final In data){
        return new Async<In, Out>(new Callable<In>() {
            @Override
            public In call() throws Exception {
                return data;
            }
        });
    }
    public static <In, Out> AsyncList<In, Out> justMulti(List<In> list){
        return new AsyncList<In, Out>(new Callable<List<In>>() {
            @Override
            public List<In> call() throws Exception {
                return list;
            }
        });
    }

    /**
     * create a list from int start and to.
     * @param from the from index (include)
     * @param to the end index (exclude)
     * @param <Out> the out type
     * @return the async list
     */
    public static <Out> AsyncList<Integer, Out> justMulti(int from, int to){
        return new AsyncList<Integer, Out>(new Callable<List<Integer>>() {
            @Override
            public List<Integer> call() throws Exception {
                List<Integer> list = new ArrayList<>();
                for (int i = from ; i < to ; i ++){
                    list.add(i);
                }
                return list;
            }
        });
    }
}
