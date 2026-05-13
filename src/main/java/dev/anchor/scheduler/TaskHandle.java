package dev.anchor.scheduler;

public interface TaskHandle {

    int taskId();

    void cancel();

    boolean isCancelled();
}
