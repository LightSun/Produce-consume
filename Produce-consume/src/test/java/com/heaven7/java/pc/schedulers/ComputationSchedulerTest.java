package com.heaven7.java.pc.schedulers;

public class ComputationSchedulerTest extends BaseSchedulerTest {

    @Override
    protected void setUp() {
        super.setUp();
        mScheduler = new ComputationScheduler();
    }
}
