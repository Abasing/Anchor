package me.zamin.anchor.api.scheduler;

/**
 * Callback invoked when a task completes, is cancelled, or fails.
 */
@FunctionalInterface
public interface TaskCallback {

    void accept(TaskHandle handle, Throwable throwable);
}
