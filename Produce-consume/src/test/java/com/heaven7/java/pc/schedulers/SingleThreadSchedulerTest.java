package com.heaven7.java.pc.schedulers;

public class SingleThreadSchedulerTest extends BaseSchedulerTest {

    @Override
    protected void setUp() {
        super.setUp();
        mScheduler = new SingleThreadScheduler();
    }

}
