package com.heaven7.java.pc.internal;

import com.heaven7.java.base.util.Scheduler;
import com.heaven7.java.pc.Producer;
import com.heaven7.java.pc.ProductContext;
import com.heaven7.java.pc.TaskNode;
import com.heaven7.java.pc.producers.BaseProducer;

import java.util.Iterator;
import java.util.List;

/**
 * @author heaven7
 */
public class Utils {

    public static <T> TaskNode<T> generateOrderedTasks(BaseProducer<T> producer, Iterable<T> ita,
                                                       ProductContext context, Scheduler scheduler, Producer.Callback<T> callback){
        final Iterator<T> it = ita.iterator();
        TaskNode<T> head = producer.createTaskNode(context, scheduler, callback);
        TaskNode<T> last = head;
        if(it.hasNext()){
            last.current = it.next();
        }
        while (it.hasNext()){
            TaskNode<T> task = producer.createTaskNode(context, scheduler, callback);
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
            return producer.createTaskNode(null, null, null);
        }
        //create head
        TaskNode<T> head = producer.createTaskNode(context, scheduler, callback);
        TaskNode<T> last = head;
        last.current = list.get(0);

        for(int i = 1; i < size ; i ++){
            TaskNode<T> task = producer.createTaskNode(context, scheduler, callback);
            task.current = list.get(i);
            last.nextTask = task;
            last = task;
        }
        return head;
    }
}
