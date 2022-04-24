package com.heaven7.java.pc.async;

import com.heaven7.java.base.anno.Nullable;
import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.pc.ProductContext;
import com.heaven7.java.pc.Transformer;
import com.heaven7.java.pc.Transformer2;
import com.heaven7.java.pc.Transformers;
import com.heaven7.java.pc.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
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
    protected Scheduler scheduler;

    private Disposable scheduleDispose;
    protected Disposable observeDispose;

    private final AtomicReference<Response<?>> mResultRef = new AtomicReference<>();
    protected Transformer<In, Out> transformer = (Transformer<In, Out>) Transformers.unchangeTransformer();
    private Action<Out> resultTask;
    private Action<Throwable> exceptionTask;
    private ProductContext context;

    private final List<Disposable> mDisposeTasks = new ArrayList<>(4);

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

    /**
     * add task to do on cancel.
     * @param task the dispose task
     * @since 1.0.5
     */
    public void addDisposeTask(Disposable task) {
        mDisposeTasks.add(task);
    }

    public Response getResponse(){
        return mResultRef.get();
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
    public Async<In, Out> transformer(Transformer<? super In, Out> transformer){
        this.transformer = (Transformer<In, Out>) transformer;
        return this;
    }

    /**
     * extend method used by subs
     * @param transformer the transformer
     * @param p the extra param
     * @return this
     * @since 1.0.5
     */
    public Async<In, Out> transformer(Transformer transformer, Object p){
        throw new UnsupportedOperationException();
    }
    /**
     * extend method used by subs
     * @param transformer the transformer
     * @param p the extra param
     * @return this
     * @since 1.0.5
     */
    public Async<In, Out> transformer2(Transformer2 transformer, Object p){
        throw new UnsupportedOperationException();
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
        for (Disposable d: mDisposeTasks) {
            d.dispose();
        }
        mDisposeTasks.clear();
    }
    public void then(Action<Out> task, @Nullable Scheduler observe){
        result(task).start(observe);
    }
    public void then(Action<Out> task){
        result(task).start(null);
    }
    public void start(){
        start(null);
    }
    public void start(@Nullable final Scheduler observe){
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
    }
    private void run0(@Nullable Scheduler observe){
        In result = null;
        Throwable exp = null;
        if(actTask != null){
            try {
                result = actTask.call();
            } catch (Throwable e) {
                exp = e;
            }
        }
        scheduleImpl(observe, result, exp);
    }
    protected void callback(In result, Throwable e0){
        if(manager != null){
            manager.removeDisposable(this);
        }
        if(e0 != null){
            callbackException(e0);
        }else if(mResultRef.get() != null && mResultRef.get().getThrowable() != null){
            callbackException0(mResultRef.get());
        } else{
            try {
                resultTask.run(saveResult(Response.ofSuccess(transformer.transform(context, result))));
            }catch (Throwable e){
                callbackException(e);
            }
        }
    }
    protected <R> Response<R> saveResult(Response<R> response){
        if(!mResultRef.compareAndSet(null, response)){
           // System.err.println("saveResult failed");
        }
        return response;
    }
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
    protected void callbackException0(Response res){
        if(exceptionTask != null){
            exceptionTask.run(res);
        }else{
            Throwable e = res.getThrowable();
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

    //--------------- protected -----------
    protected void scheduleImpl(@Nullable Scheduler observe, In result, Throwable exp){
        if(observe == null){
            callback(result, exp);
        }else {
            final In result1 = result;
            final Throwable exp1 = exp;
            observeDispose = observe.newWorker().schedule(new Runnable() {
                @Override
                public void run() {
                    callback(result1, exp1);
                }
            });
        }
    }
}
