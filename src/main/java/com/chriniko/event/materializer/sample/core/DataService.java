package com.chriniko.event.materializer.sample.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataService<T, O, K> {

    private final Iterable<Builder<T, O, K>> builders;
    private final ScheduledExecutorService scheduledExecutorService;
    private final long synchronizeDelayMS;
    private final ForkJoinPool forkJoinPool;

    public DataService(Iterable<Builder<T, O, K>> builders, long synchronizeDelayMS) {
        this.builders = builders;
        this.synchronizeDelayMS = synchronizeDelayMS;
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
        this.forkJoinPool = new ForkJoinPool();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduledExecutorService.shutdown();
            forkJoinPool.shutdown();
        }));

    }

    public void start() {
        Runnable task = () -> {
            for (Builder<T, O, K> builder : builders) {
                forkJoinPool.submit(builder::synchronize);
            }
        };
        scheduledExecutorService.scheduleWithFixedDelay(task, 1000, synchronizeDelayMS, TimeUnit.MILLISECONDS);
    }
}
