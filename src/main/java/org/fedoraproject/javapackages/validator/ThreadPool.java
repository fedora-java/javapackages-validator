package org.fedoraproject.javapackages.validator;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public final class ThreadPool {
    private static final class Lazy {
        private static final ForkJoinPool INSTANCE = new ForkJoinPool();
    }

    private ThreadPool() {
    }

    public static synchronized Future<?> submit(Runnable task) {
        return Lazy.INSTANCE.submit(task);
    }

    public static synchronized <T> Future<T> submit(Callable<T> task) {
        return Lazy.INSTANCE.submit(task);
    }

    public static synchronized int getParallelism() {
        return Lazy.INSTANCE.getParallelism();
    }
}
