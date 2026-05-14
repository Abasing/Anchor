package me.zamin.anchor.internal;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import me.zamin.anchor.api.ServiceStatus;
import me.zamin.anchor.api.scheduler.AsyncTaskScheduler;
import me.zamin.anchor.api.scheduler.SchedulerDiagnostics;
import me.zamin.anchor.api.scheduler.SchedulerPlatform;
import me.zamin.anchor.api.scheduler.SchedulerService;
import me.zamin.anchor.api.scheduler.TaskHandle;
import me.zamin.anchor.api.scheduler.TaskScheduler;
import me.zamin.anchor.internal.metrics.AnchorMetrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class BukkitSchedulerAdapter implements SchedulerService {

    private final Plugin plugin;
    private final AnchorMetrics metrics;
    private final TaskScheduler global;
    private final AsyncTaskScheduler async;
    private final SchedulerDiagnostics diagnostics;

    public BukkitSchedulerAdapter(Plugin plugin, boolean foliaDetected, AnchorMetrics metrics) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
        this.global = new BukkitTaskScheduler(false, "global");
        this.async = new BukkitAsyncTaskScheduler();
        this.diagnostics = new SchedulerDiagnostics(
            SchedulerPlatform.BUKKIT,
            foliaDetected,
            true,
            false,
            false,
            true,
            "BukkitSchedulerAdapter",
            List.of(
                "Region and entity schedulers fall back to the global Bukkit scheduler on non-Folia platforms.",
                "Use Anchor's scheduler API instead of BukkitScheduler directly to prepare for Folia migration."
            )
        );
    }

    @Override
    public TaskScheduler global() {
        return global;
    }

    @Override
    public AsyncTaskScheduler async() {
        return async;
    }

    @Override
    public TaskScheduler region(Location location) {
        return global;
    }

    @Override
    public TaskScheduler entity(Entity entity) {
        return global;
    }

    @Override
    public SchedulerPlatform platform() {
        return SchedulerPlatform.BUKKIT;
    }

    @Override
    public SchedulerDiagnostics diagnostics() {
        return diagnostics;
    }

    @Override
    public void cancel(TaskHandle handle) {
        if (handle != null) {
            handle.cancel();
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String providerName() {
        return "BukkitSchedulerAdapter";
    }

    @Override
    public ServiceStatus status() {
        return ServiceStatus.AVAILABLE;
    }

    private class BukkitTaskScheduler implements TaskScheduler {

        private final boolean asynchronous;
        private final String metricContext;

        private BukkitTaskScheduler(boolean asynchronous, String metricContext) {
            this.asynchronous = asynchronous;
            this.metricContext = metricContext;
        }

        @Override
        public TaskHandle run(Runnable task) {
            SimpleTaskHandle handle = new SimpleTaskHandle(false, metrics, metricContext);
            BukkitTask scheduled = asynchronous
                ? Bukkit.getScheduler().runTaskAsynchronously(plugin, wrap(handle, task, false))
                : Bukkit.getScheduler().runTask(plugin, wrap(handle, task, false));
            handle.setTaskId(scheduled.getTaskId());
            handle.setCancelAction(() -> {
                scheduled.cancel();
                metrics.increment("scheduler.cancelled." + metricContext);
            });
            return handle;
        }

        @Override
        public TaskHandle runLater(Runnable task, long delayTicks) {
            SimpleTaskHandle handle = new SimpleTaskHandle(false, metrics, metricContext);
            BukkitTask scheduled = asynchronous
                ? Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, wrap(handle, task, false), delayTicks)
                : Bukkit.getScheduler().runTaskLater(plugin, wrap(handle, task, false), delayTicks);
            handle.setTaskId(scheduled.getTaskId());
            handle.setCancelAction(() -> {
                scheduled.cancel();
                metrics.increment("scheduler.cancelled." + metricContext);
            });
            return handle;
        }

        @Override
        public TaskHandle runRepeating(Runnable task, long delayTicks, long periodTicks) {
            SimpleTaskHandle handle = new SimpleTaskHandle(true, metrics, metricContext);
            BukkitTask scheduled = asynchronous
                ? Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, wrap(handle, task, true), delayTicks, periodTicks)
                : Bukkit.getScheduler().runTaskTimer(plugin, wrap(handle, task, true), delayTicks, periodTicks);
            handle.setTaskId(scheduled.getTaskId());
            BukkitTask finalScheduled = scheduled;
            handle.setCancelAction(() -> {
                finalScheduled.cancel();
                metrics.increment("scheduler.cancelled." + metricContext);
                handle.markCancelled();
            });
            return handle;
        }

        private Runnable wrap(SimpleTaskHandle handle, Runnable task, boolean repeating) {
            long submittedAt = System.nanoTime();
            AtomicBoolean firstExecution = new AtomicBoolean(true);
            return () -> {
                long startedAt = System.nanoTime();
                if (firstExecution.compareAndSet(true, false)) {
                    metrics.recordTiming("scheduler.delay." + metricContext, startedAt - submittedAt);
                }
                try {
                    task.run();
                    metrics.increment("scheduler.executed." + metricContext);
                    if (!repeating) {
                        handle.markSuccess();
                    }
                } catch (Throwable throwable) {
                    metrics.increment("scheduler.failed." + metricContext);
                    handle.markFailure(throwable);
                    handle.cancel();
                } finally {
                    metrics.increment("scheduler.submitted." + metricContext);
                    metrics.recordTiming("scheduler.runtime." + metricContext, System.nanoTime() - startedAt);
                }
            };
        }
    }

    private final class BukkitAsyncTaskScheduler extends BukkitTaskScheduler implements AsyncTaskScheduler {

        private BukkitAsyncTaskScheduler() {
            super(true, "async");
        }

        @Override
        public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
            CompletableFuture<T> future = new CompletableFuture<>();
            long submittedAt = System.nanoTime();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                long startedAt = System.nanoTime();
                metrics.recordTiming("scheduler.delay.asyncFuture", startedAt - submittedAt);
                try {
                    future.complete(supplier.get());
                    metrics.increment("scheduler.executed.asyncFuture");
                } catch (Throwable throwable) {
                    metrics.increment("scheduler.failed.asyncFuture");
                    future.completeExceptionally(throwable);
                } finally {
                    metrics.increment("scheduler.submitted.asyncFuture");
                    metrics.recordTiming("scheduler.runtime.asyncFuture", System.nanoTime() - startedAt);
                }
            });
            return future;
        }
    }
}
