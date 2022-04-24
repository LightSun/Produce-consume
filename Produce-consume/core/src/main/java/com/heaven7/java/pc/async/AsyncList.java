package com.heaven7.java.pc.async;

import com.heaven7.java.base.util.Predicates;
import com.heaven7.java.pc.ProductContext;
import com.heaven7.java.pc.Transformer;
import com.heaven7.java.pc.utils.Pair;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * transform list to list
 * @param <In> the in type
 * @param <Out> the out type
 * @since 1.0.5
 */
public class AsyncList<In, Out> extends Async<List<In>, List<Out>>{

    private int mEverySize = 1;

    public AsyncList(Callable<List<In>> actTask) {
        super(actTask);
    }

    public AsyncList(AsyncManager manager, Callable<List<In>> actTask) {
        super(manager, actTask);
    }

    /**
     * if you want to group list
     * @param everySize the every size
     * @return this
     */
    public AsyncList<In, Out> everySize(int everySize) {
        mEverySize = everySize;
        return this;
    }

    /**
     * extend transformer.
     * @param transformer the transformer should be 'Transformer<List<In>, Out>'
     * @param param true if in order
     * @return this
     */
    @Override
    @SuppressWarnings("unchecked")
    public AsyncList<In, Out> transformer2(Transformer transformer, Object param) {
        final boolean inOrder = Predicates.isTrue(param);
        this.transformer = new Transformer<List<In>, List<Out>>() {
            @Override
            public List<Out> transform(ProductContext context, List<In> ins) {
                if(mEverySize > 0){
                    int c = ins.size() / mEverySize + (ins.size() % mEverySize != 0 ? 1 : 0);

                    int start;
                    if(scheduler != null){
                        final List<Pair<Integer, Out>> pairs = new ArrayList<>();
                        CountDownLatch ldt = new CountDownLatch(c);
                        for (int i = 0 ; i < c ; i ++){
                            System.out.println("transformer2:  i = " + i);
                            start = i * mEverySize;
                            final int index = i;
                            final List<In> list = ins.subList(start, i != c - 1 ? start + mEverySize : ins.size());
                            addDisposeTask(scheduler.newWorker().schedule(new Runnable() {
                                @Override
                                public void run() {
                                    System.out.println("transformer2:  done index = " + index);
                                    try{
                                        Out out = (Out) transformer.transform(context, list);
                                        pairs.add(new Pair<Integer, Out>(index, out));
                                    }finally {
                                        ldt.countDown();
                                    }
                                }
                            }));
                        }
                        try {
                            ldt.await();
                            if(inOrder){
                                pairs.sort(CMP_PAIR_INDEX);
                            }
                            List<Out> result = new ArrayList<>();
                            for (Pair<Integer, Out> p : pairs){
                                result.add(p.value);
                            }
                            return result;
                        } catch (InterruptedException e) {
                            saveResult(Response.ofThrowable(e));
                            //ignore
                            return Collections.emptyList();
                        }
                    }else{
                        final List<Out> result = new ArrayList<>();
                        for (int i = 0 ; i < c ; i ++){
                            start = i * mEverySize;
                            final List<In> list = ins.subList(start, i != c - 1 ? start + mEverySize : ins.size());
                            result.add((Out) transformer.transform(context, list));
                        }
                        return result;
                    }
                }
                return Arrays.asList((Out)transformer.transform(context, ins));
            }
        };
        return this;
    }
    private static final Comparator<Pair<Integer, ?>> CMP_PAIR_INDEX = new Comparator<Pair<Integer, ?>>() {
        @Override
        public int compare(Pair<Integer, ?> o1, Pair<Integer, ?> o2) {
            return Integer.compare(o1.key, o2.key);
        }
    };
}
