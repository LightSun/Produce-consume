package com.heaven7.java.pc.async;

import com.heaven7.java.base.util.Disposable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author heaven7
 * @since 1.0.5
 */
public final class AsyncManager {

    private final List<Disposable> mList = new CopyOnWriteArrayList<>();

    public void addDisposable(Disposable disposable){
        mList.add(disposable);
    }

    public void removeDisposable(Disposable disposable){
        mList.remove(disposable);
    }
    public void cancel(){
        final Object[] objs = mList.toArray();
        mList.clear();
        for (Object dis : objs){
            ((Disposable)dis).dispose();
        }
    }
}
