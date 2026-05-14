package me.zamin.anchor.internal;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import me.zamin.anchor.api.scheduler.TaskCallback;
import me.zamin.anchor.api.scheduler.TaskHandle;
import me.zamin.anchor.internal.metrics.AnchorMetrics;

final class SimpleTaskHandle implements TaskHandle {

    private final AtomicInteger taskId = new AtomicInteger(-1);
    private final boolean repeating;
    private final CompletableFuture<Void> completionFuture = new CompletableFuture<>();
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AnchorMetrics metrics;
    private final String metricContext;
    private Runnable cancelAction = () -> {
    };

    SimpleTaskHandle(boolean repeating, AnchorMetrics metrics, String metricContext) {
        this.repeating = repeating;
        this.metrics = metrics;
        this.metricContext = metricContext;
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

    @Override
    public TaskHandle onComplete(TaskCallback callback) {
        completionFuture().whenComplete((ignored, throwable) -> {
            long start = System.nanoTime();
            try {
                callback.accept(this, throwable);
            } finally {
                metrics.recordTiming("scheduler.callback." + metricContext, System.nanoTime() - start);
            }
        });
        return this;
    }
}
