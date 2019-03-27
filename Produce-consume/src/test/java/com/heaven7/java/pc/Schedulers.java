package com.heaven7.java.pc;

import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.base.util.threadpool.Executors2;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Schedulers {

	public static final Disposable EMPTY = new Disposable() {
		@Override
		public void dispose() {

		}
	};
	public static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final Scheduler DEFAULT = new DefaultScheduler();
	public static final Scheduler GROUP_ASYNC = new GroupAsyncScheduler();

	public static String getCurrentTime(){
		return DF.format(new Date(System.currentTimeMillis()));
	}
	
    private static class GroupAsyncScheduler implements Scheduler{
		@Override
		public Worker newWorker() {
			return new GroupWorker();
		}
	}
	private static class GroupWorker implements Scheduler.Worker{
		final ScheduledExecutorService pool = Executors2.newScheduledThreadPool(5);
		@Override
		public Disposable scheduleDelay(final Runnable task, long delay, TimeUnit unit) {
			return new Disposable.FutureDisposable(pool.schedule(task, delay, unit));
		}
		@Override
		public Disposable schedulePeriodically(Runnable task, long initDelay, long period, TimeUnit unit) {
			return new Disposable.FutureDisposable(pool.scheduleAtFixedRate(task, initDelay, period, unit));
		}
		@Override
		public Disposable schedule(final Runnable task) {
			return new Disposable.FutureDisposable(pool.submit(task));
		}
	}
	private static class DefaultScheduler implements Scheduler{
		@Override
		public Worker newWorker() {
			return new DefaultWorker();
		}

	}
	private static class DefaultWorker implements Scheduler.Worker{
		@Override
		public Disposable scheduleDelay(Runnable task, long delay, TimeUnit unit) {
			try {
				Thread.sleep(delay);
				task.run();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return EMPTY;
		}
		@Override
		public Disposable schedulePeriodically(Runnable task, long initDelay, long period, TimeUnit unit) {
			try {
				Thread.sleep(unit.toMillis(initDelay));
				do {
					task.run();
					Thread.sleep(unit.toMillis(period));
				}while (true);
			}catch (InterruptedException e){
				e.printStackTrace();
			}
			return EMPTY;
		}
		@Override
		public Disposable schedule(Runnable task) {
			task.run();
			return EMPTY;
		}
	}
	
}
