package me.zamin.anchor.api.scheduler;

/**
 * Callback invoked when a task completes, is cancelled, or fails.
 */
@FunctionalInterface
public interface TaskCallback {

    /**
     * Receives task completion notification.
     *
     * @param handle non-null task handle
     * @param throwable null on success or normal cancellation, otherwise the failure
     */
    void accept(TaskHandle handle, Throwable throwable);
}
