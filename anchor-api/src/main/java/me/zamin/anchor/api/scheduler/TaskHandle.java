package me.zamin.anchor.api.scheduler;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a scheduled task managed by Anchor.
 */
public interface TaskHandle {

    int taskId();

    void cancel();

    boolean isCancelled();

    boolean isRepeating();

    CompletableFuture<Void> completionFuture();

    default TaskHandle onComplete(TaskCallback callback) {
        Objects.requireNonNull(callback, "callback");
        completionFuture().whenComplete((ignored, throwable) -> callback.accept(this, throwable));
        return this;
    }
}
