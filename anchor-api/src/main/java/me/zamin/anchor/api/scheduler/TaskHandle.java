package me.zamin.anchor.api.scheduler;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a scheduled task managed by Anchor.
 */
public interface TaskHandle {

    /**
     * Returns the underlying runtime task identifier when available.
     *
     * @return implementation task identifier, or a synthetic identifier on some platforms
     */
    int taskId();

    /**
     * Cancels the task.
     * <p>
     * Safe to call on Paper and Folia. Cancellation is best-effort for tasks
     * that are already executing.
     */
    void cancel();

    /**
     * Returns whether the task has been cancelled.
     *
     * @return {@code true} if cancelled
     */
    boolean isCancelled();

    /**
     * Returns whether the task repeats until cancelled.
     *
     * @return {@code true} for repeating tasks
     */
    boolean isRepeating();

    /**
     * Returns a completion future for the task.
     * <p>
     * Repeating tasks complete when cancelled. One-shot tasks complete when the
     * task finishes or fails.
     *
     * @return non-null completion future
     */
    CompletableFuture<Void> completionFuture();

    /**
     * Registers a completion callback.
     *
     * @param callback non-null callback
     * @return this handle
     */
    default TaskHandle onComplete(TaskCallback callback) {
        Objects.requireNonNull(callback, "callback");
        completionFuture().whenComplete((ignored, throwable) -> callback.accept(this, throwable));
        return this;
    }
}
