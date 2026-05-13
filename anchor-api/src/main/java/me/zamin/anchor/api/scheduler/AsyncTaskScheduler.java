package me.zamin.anchor.api.scheduler;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Async scheduling surface that supports future-based execution helpers.
 */
public interface AsyncTaskScheduler extends TaskScheduler {

    <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier);

    default CompletableFuture<Void> runAsync(Runnable task) {
        return supplyAsync(() -> {
            task.run();
            return null;
        });
    }
}
