package com.heaven7.java.pc.schedulers;

import com.heaven7.java.pc.internal.Config;
import com.heaven7.java.pc.internal.Utils;

/**
 * the task looper which was time limited.
 * @author heaven7
 */
public class LimitTimeDelayTaskLooper extends DelayHelper implements DelayTaskLooper{

    private static final long DEFAULT_KEEP_TIME;

    private final Runnable mKeepTimeEnd;
    private final long mKeepTime; //in mills
    private long mTriggerTime;

    static {
        Long time = Long.getLong(Config.KEY_TIME_LIMIT_LOOP_KEEP_TIME);
        DEFAULT_KEEP_TIME = time != null ? time : -1;
    }
    public LimitTimeDelayTaskLooper(boolean disposeImmediately, Runnable mEnd) {
        this(DEFAULT_KEEP_TIME, disposeImmediately, mEnd);
    }

    public LimitTimeDelayTaskLooper(long mKeepTime, boolean disposeImmediately, Runnable keepTimeEnd) {
        super(disposeImmediately);
        if(mKeepTime <= 0){
            throw new IllegalArgumentException();
        }
        this.mKeepTime = mKeepTime;
        this.mKeepTimeEnd = keepTimeEnd;
    }

    @Override
    protected void onEndLoop() {
        //time reached but not cancelled.
        if(!isCancelled()){
            if (mKeepTimeEnd != null) {
                mKeepTimeEnd.run();
            }
        }
    }
    @Override
    protected void onStartLoop() {
        mTriggerTime = Utils.currentTimeMillis();
    }
    @Override
    protected boolean shouldExitLoop() {
        if(super.shouldExitLoop()){
            return true;
        }
        return mKeepTime > 0 && (Utils.currentTimeMillis() - mTriggerTime) >= mKeepTime;
    }
}
