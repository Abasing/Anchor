package me.zamin.anchor.api.scheduler;

/**
 * Stable scheduling surface for a specific execution context.
 */
public interface TaskScheduler {

    /**
     * Runs a task as soon as possible in this scheduler context.
     *
     * @param task non-null task
     * @return non-null task handle
     */
    TaskHandle run(Runnable task);

    /**
     * Runs a task after a delay measured in ticks.
     *
     * @param task non-null task
     * @param delayTicks delay in ticks
     * @return non-null task handle
     */
    TaskHandle runLater(Runnable task, long delayTicks);

    /**
     * Runs a repeating task in this scheduler context.
     *
     * @param task non-null task
     * @param delayTicks initial delay in ticks
     * @param periodTicks repeat period in ticks
     * @return non-null task handle
     */
    TaskHandle runRepeating(Runnable task, long delayTicks, long periodTicks);
}
