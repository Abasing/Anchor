package me.zamin.anchor.api.scheduler;

/**
 * Stable scheduling surface for a specific execution context.
 */
public interface TaskScheduler {

    TaskHandle run(Runnable task);

    TaskHandle runLater(Runnable task, long delayTicks);

    TaskHandle runRepeating(Runnable task, long delayTicks, long periodTicks);
}
