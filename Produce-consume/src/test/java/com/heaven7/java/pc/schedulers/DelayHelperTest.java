package com.heaven7.java.pc.schedulers;

public class DelayHelperTest extends BaseLooperTest {

    @Override
    protected DelayTaskLooper createLooper() {
        return new DelayHelper();
    }
}
