package com.fast.cqrs.concurrent;

import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Virtual Thread executor for concurrent operations.
 * <p>
 * Simple API for running tasks on Virtual Threads (Java 21+).
 * <p>
 * Usage:
 * <pre>{@code
 * import static com.fast.cqrs.concurrent.VirtualThread.*;
 * 
 * // Run and get result
 * var user = get(run(() -> userService.findById(id)));
 * 
 * // Parallel execution
 * var users = parallel(
 *     () -> fetchUser(1),
 *     () -> fetchUser(2),
 *     () -> fetchUser(3)
 * );
 * 
 * // With timeout
 * var result = timeout(() -> slowService.call(), 5, TimeUnit.SECONDS);
 * 
 * // With retry
 * var result = retry(() -> unreliableService.call(), 3);
 * }</pre>
 */
public final class VirtualThread {

    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private VirtualThread() {}

    // ========== Basic Operations ==========

    /**
     * Runs a task on a Virtual Thread.
     */
    public static <T> Future<T> run(Supplier<T> task) {
        return EXECUTOR.submit(task::get);
    }

    /**
     * Runs a Callable on a Virtual Thread.
     */
    public static <T> Future<T> run(Callable<T> task) {
        return EXECUTOR.submit(task);
    }

    /**
     * Executes a task (fire-and-forget).
     */
    public static void execute(Runnable task) {
        EXECUTOR.execute(task);
    }

    /**
     * Gets result from Future.
     */
    public static <T> T get(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    // ========== Parallel Execution ==========

    /**
     * Runs multiple tasks in parallel and returns all results.
     */
    @SafeVarargs
    public static <T> java.util.List<T> parallel(Supplier<T>... tasks) {
        var futures = new java.util.ArrayList<Future<T>>();
        for (var task : tasks) {
            futures.add(run(task));
        }
        var results = new java.util.ArrayList<T>();
        for (var future : futures) {
            results.add(get(future));
        }
        return results;
    }

    /**
     * Runs first completed task (race).
     */
    @SafeVarargs
    public static <T> T race(Supplier<T>... tasks) {
        var futures = new java.util.ArrayList<Future<T>>();
        for (var task : tasks) {
            futures.add(run(task));
        }
        while (true) {
            for (var future : futures) {
                if (future.isDone()) {
                    futures.forEach(f -> f.cancel(true));
                    return get(future);
                }
            }
            sleep(1);
        }
    }

    // ========== Timeout ==========

    /**
     * Runs task with timeout.
     */
    public static <T> T timeout(Supplier<T> task, long timeout, TimeUnit unit) {
        var future = run(task);
        try {
            return future.get(timeout, unit);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("Timeout after " + timeout + " " + unit, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    /**
     * Runs task with timeout in seconds.
     */
    public static <T> T timeout(Supplier<T> task, long seconds) {
        return timeout(task, seconds, TimeUnit.SECONDS);
    }

    // ========== Retry ==========

    /**
     * Runs task with retry.
     */
    public static <T> T retry(Supplier<T> task, int maxAttempts) {
        return retry(task, maxAttempts, 100);
    }

    /**
     * Runs task with retry and delay.
     */
    public static <T> T retry(Supplier<T> task, int maxAttempts, long delayMs) {
        Exception lastException = null;
        for (int i = 0; i < maxAttempts; i++) {
            try {
                return task.get();
            } catch (Exception e) {
                lastException = e;
                if (i < maxAttempts - 1) {
                    sleep(delayMs);
                }
            }
        }
        throw new RuntimeException("Failed after " + maxAttempts + " attempts", lastException);
    }

    // ========== Utilities ==========

    /**
     * Sleeps current thread.
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Delays with callback.
     */
    public static void delay(long millis, Runnable callback) {
        execute(() -> {
            sleep(millis);
            callback.run();
        });
    }

    /**
     * Schedules periodic execution.
     */
    public static void interval(long periodMs, Runnable task) {
        execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                task.run();
                sleep(periodMs);
            }
        });
    }

    /**
     * Gets executor.
     */
    public static ExecutorService executor() {
        return EXECUTOR;
    }
}
