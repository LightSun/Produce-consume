package com.heaven7.java.pc.schedulers;

import com.heaven7.java.base.anno.NeedAndroidImpl;
import com.heaven7.java.base.util.Platforms;
import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.base.util.threadpool.Executors2;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

import static com.heaven7.java.pc.schedulers.Schedulers.ANDROID_MAIN;


/**
 * the scheduler factory. which can create multi type schedulers.
 * @author heaven7
 */
@NeedAndroidImpl(ANDROID_MAIN)
public final class Schedulers {

    static final String ANDROID_MAIN = "com.heaven7.java.pc.schedulers.AndroidMainScheduler";
    private Schedulers(){}

    private static class IoHolder{
        static final Scheduler DEFAULT  = new IoScheduler();
    }
    private static class ComputeHolder{
        static final Scheduler DEFAULT  = new ComputationScheduler();
    }
    private static class SingleHolder{
        static final Scheduler DEFAULT  = new SingleThreadScheduler();
    }
    private static class MainHolder{
        static final Scheduler DEFAULT;
        static {
            try {
                DEFAULT = (Scheduler) Class.forName(ANDROID_MAIN).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new RuntimeException("can't load class '" + ANDROID_MAIN + "'", e);
            }
        }
    }
    public static Scheduler main(){
        if(!Platforms.isAndroid()){
            throw new UnsupportedOperationException("only android support main scheduler.");
        }
        return MainHolder.DEFAULT;
    }
    public static Scheduler io(){
        return IoHolder.DEFAULT;
    }
    public static Scheduler compute(){
        return ComputeHolder.DEFAULT;
    }
    public static Scheduler single(){
        return SingleHolder.DEFAULT;
    }
    public static Scheduler newCached(){
        return new CommonScheduler(Executors2.newCachedThreadPool());
    }
    public static Scheduler scheduleExecutor(ScheduledExecutorService service){
        return new ScheduleExecutorScheduler(service);
    }
    public static Scheduler common(DelayTaskLooper looper, Executor executor){
        return new CommonScheduler(looper, executor);
    }
    public static Scheduler common(Executor executor){
        return new CommonScheduler(executor);
    }
}
