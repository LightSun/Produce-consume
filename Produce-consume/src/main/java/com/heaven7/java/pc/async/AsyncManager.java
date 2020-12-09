package com.heaven7.java.pc.async;

import com.heaven7.java.base.util.Disposable;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author heaven7
 * @since 1.0.5
 */
public final class AsyncManager {

    private final Set<Disposable> mList = new CopyOnWriteArraySet<>();

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
