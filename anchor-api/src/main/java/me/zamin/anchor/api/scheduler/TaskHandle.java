package me.zamin.anchor.api.scheduler;

public interface TaskHandle {

    int taskId();

    void cancel();

    boolean isCancelled();
}
