package me.zamin.anchor.internal;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import me.zamin.anchor.api.scheduler.TaskHandle;

final class SimpleTaskHandle implements TaskHandle {

    private final AtomicInteger taskId = new AtomicInteger(-1);
    private final boolean repeating;
    private final CompletableFuture<Void> completionFuture = new CompletableFuture<>();
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private Runnable cancelAction = () -> {
    };

    SimpleTaskHandle(boolean repeating) {
        this.repeating = repeating;
    }

    void setTaskId(int id) {
        this.taskId.set(id);
    }

    void setCancelAction(Runnable cancelAction) {
        this.cancelAction = cancelAction;
    }

    void markSuccess() {
        if (!repeating && !completionFuture.isDone()) {
            completionFuture.complete(null);
        }
    }

    void markFailure(Throwable throwable) {
        if (!completionFuture.isDone()) {
            completionFuture.completeExceptionally(throwable);
        }
    }

    void markCancelled() {
        cancelled.set(true);
        if (!completionFuture.isDone()) {
            completionFuture.complete(null);
        }
    }

    @Override
    public int taskId() {
        return taskId.get();
    }

    @Override
    public void cancel() {
        cancelAction.run();
        markCancelled();
    }

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }

    @Override
    public boolean isRepeating() {
        return repeating;
    }

    @Override
    public CompletableFuture<Void> completionFuture() {
        return completionFuture;
    }
}
