package com.heaven7.java.pc.schedulers;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class SingleThreadSchedulerTest extends BaseSchedulerTest {

    @Override
    protected void setUp() {
        super.setUp();
        mScheduler = new SingleThreadScheduler();
    }

}
