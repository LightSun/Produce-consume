package com.heaven7.java.pc.internal;

import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.pc.BaseProducer;
import com.heaven7.java.pc.Producer;
import com.heaven7.java.pc.ProductContext;
import com.heaven7.java.pc.TaskNode;

import java.util.Iterator;
import java.util.List;

/**
 * @author heaven7
 */
public final class Utils {

    public static long currentTimeMillis(){
        return System.currentTimeMillis(); // for android may not the same
    }
/*
    public static Float getFloat(String prop){
        String v = null;
        try {
            v = System.getProperty(prop);
        } catch (IllegalArgumentException | NullPointerException e) {
        }
        if (v != null) {
            try {
                return Float.valueOf(v);
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }*/

    public static <T> TaskNode<T> generateOrderedTasks(BaseProducer<T> producer, Iterable<T> ita,
                                                       ProductContext context, Scheduler scheduler, Producer.Callback<T> callback){
        final Iterator<T> it = ita.iterator();
        TaskNode<T> head = new TaskNode<>(producer,context, scheduler, callback);
        TaskNode<T> last = head;
        if(it.hasNext()){
            last.current = it.next();
        }
        while (it.hasNext()){
            TaskNode<T> task = new TaskNode<>(producer,context, scheduler, callback);
            task.current = it.next();
            last.nextTask = task;
            last = task;
        }
        return head;
    }
    public static <T> TaskNode<T> generateOrderedTasks(BaseProducer<T> producer, List<T> list,
                                                       ProductContext context, Scheduler scheduler, Producer.Callback<T> callback){
        int size = list.size();
        if(size == 0){
            return new TaskNode<>(producer,null, null, null);
        }
        //create head
        TaskNode<T> head = new TaskNode<>(producer,context, scheduler, callback);
        TaskNode<T> last = head;
        last.current = list.get(0);

        for(int i = 1; i < size ; i ++){
            TaskNode<T> task = new TaskNode<>(producer,context, scheduler, callback);
            task.current = list.get(i);
            last.nextTask = task;
            last = task;
        }
        return head;
    }
}
