package me.zamin.anchor.api.scheduler;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Async scheduling surface that supports future-based execution helpers.
 */
public interface AsyncTaskScheduler extends TaskScheduler {

    /**
     * Runs a supplier asynchronously and returns its future result.
     *
     * @param supplier non-null supplier
     * @param <T> result type
     * @return non-null future
     */
    <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier);

    /**
     * Runs a fire-and-forget async task and returns its completion future.
     *
     * @param task non-null task
     * @return non-null completion future
     */
    default CompletableFuture<Void> runAsync(Runnable task) {
        return supplyAsync(() -> {
            task.run();
            return null;
        });
    }
}
