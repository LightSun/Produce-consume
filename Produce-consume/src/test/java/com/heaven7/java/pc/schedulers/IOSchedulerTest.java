package com.heaven7.java.pc.schedulers;

public class IOSchedulerTest extends BaseSchedulerTest {

    @Override
    protected void setUp() {
        super.setUp();
        mScheduler = new IoScheduler();
    }
}
