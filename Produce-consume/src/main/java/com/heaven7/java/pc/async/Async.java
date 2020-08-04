package com.heaven7.java.pc.async;

import com.heaven7.java.base.anno.Nullable;
import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.pc.ProductContext;
import com.heaven7.java.pc.Transformer;
import com.heaven7.java.pc.Transformers;
import com.heaven7.java.pc.schedulers.Schedulers;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * the async executor
 * @param <In> the result type
 * @since 1.0.5
 */
public class Async<In, Out> implements Disposable{

    private final Callable<In> actTask;
    private AsyncManager manager;
    private long delay;
    private Scheduler scheduler;

    private Disposable scheduleDispose;
    private Disposable observeDispose;

    private final AtomicReference<Response> mResultRef = new AtomicReference<>();
    private Transformer<In, Out> transformer = (Transformer<In, Out>) Transformers.unchangeTransformer();
    private Action<Out> resultTask;
    private Action<Throwable> exceptionTask;
    private ProductContext context;

    public Async(Callable<In> actTask) {
        this(null, actTask);
    }
    public Async(AsyncManager manager, Callable<In> actTask) {
        this.manager = manager;
        this.actTask = actTask;
    }

    public static <In, Out> Async<In, Out> just(final In data){
        return new Async<In, Out>(new Callable<In>() {
            @Override
            public In call() throws Exception {
                return data;
            }
        });
    }

    public Response<Out> getResponse(){
        return mResultRef.get();
    }
    public Async<In, Out> then(Action<Out> task, @Nullable Scheduler observe){
        return result(task).schedule(observe);
    }
    public Async<In, Out> then(Action<Out> task){
        return result(task).schedule(null);
    }
    public Async<In, Out> manager(AsyncManager am){
        this.manager = am;
        return this;
    }

    public Async<In, Out> scheduler(Scheduler scheduler){
        this.scheduler = scheduler;
        return this;
    }
    public Async<In, Out> delay(long delayInMsec){
        this.delay = delayInMsec;
        return this;
    }
    public Async<In, Out> result(Action<Out> task){
        resultTask = task;
        return this;
    }
    public Async<In, Out> exception(Action<Throwable> task){
        exceptionTask = task;
        return this;
    }
    public Async<In, Out> transformer(Transformer<? super In, String> transformer){
        this.transformer = (Transformer<In, Out>) transformer;
        return this;
    }
    public Async<In, Out> context(ProductContext context){
        this.context = context;
        return this;
    }
    @Override
    public void dispose() {
        if(scheduleDispose != null){
            scheduleDispose.dispose();
            scheduleDispose = null;
        }
        if(observeDispose != null){
            observeDispose.dispose();
            observeDispose = null;
        }
    }
    public Async<In, Out> schedule(){
        return schedule(null);
    }
    public Async<In, Out> schedule(@Nullable final Scheduler observe){
        Runnable wrapTask = new Runnable() {
            @Override
            public void run() {
                run0(observe);
            }
        };
        if(delay > 0){
            scheduleDispose = getScheduler().newWorker().schedule(wrapTask);
        }else {
            scheduleDispose = getScheduler().newWorker().scheduleDelay(wrapTask, delay, TimeUnit.MILLISECONDS);
        }
        if(manager != null){
            manager.addDisposable(this);
        }
        return this;
    }
    private void run0(@Nullable Scheduler observe){
        In result = null;
        Exception exp = null;
        if(actTask != null){
            try {
                result = actTask.call();
            } catch (Exception e) {
                exp = e;
            }
        }
        if(observe == null){
            callback(result, exp);
        }else {
            final In result1 = result;
            final Exception exp1 = exp;
            observeDispose = observe.newWorker().schedule(new Runnable() {
                @Override
                public void run() {
                    callback(result1, exp1);
                }
            });
        }
    }
    @SuppressWarnings("unchecked")
    private void callback(In result, Throwable e0){
        if(manager != null){
            manager.removeDisposable(this);
        }
        if(e0 != null){
            callbackException(e0);
        }else{
            try {
                resultTask.run(saveResult(Response.ofSuccess(transformer.transform(context, result))));
            }catch (Throwable e){
                callbackException(e);
            }
        }
    }
    private Response saveResult(Response response){
        if(!mResultRef.compareAndSet(null, response)){
            System.err.println("saveResult failed");
        }
        return response;
    }
    @SuppressWarnings("unchecked")
    private void callbackException(Throwable e){
        if(exceptionTask != null){
            exceptionTask.run(saveResult(Response.ofThrowable(e)));
        }else{
            if(e instanceof RuntimeException){
                throw (RuntimeException)e;
            }else{
                throw new AsyncException(e);
            }
        }
    }
    //==========================================
    private Scheduler getScheduler(){
        return scheduler != null ? scheduler : Schedulers.io();
    }
}
