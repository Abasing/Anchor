package dev.anchor.scheduler;

import dev.anchor.core.AnchorService;

public interface SchedulerService extends AnchorService {

    TaskHandle runSync(Runnable runnable);

    TaskHandle runAsync(Runnable runnable);

    TaskHandle runLater(Runnable runnable, long ticks);

    TaskHandle runRepeating(Runnable runnable, long delayTicks, long periodTicks);

    void cancel(TaskHandle handle);
}
