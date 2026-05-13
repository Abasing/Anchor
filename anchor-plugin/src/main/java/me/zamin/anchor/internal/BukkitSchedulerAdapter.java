package me.zamin.anchor.internal;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import me.zamin.anchor.api.ServiceStatus;
import me.zamin.anchor.api.scheduler.AsyncTaskScheduler;
import me.zamin.anchor.api.scheduler.SchedulerDiagnostics;
import me.zamin.anchor.api.scheduler.SchedulerPlatform;
import me.zamin.anchor.api.scheduler.SchedulerService;
import me.zamin.anchor.api.scheduler.TaskHandle;
import me.zamin.anchor.api.scheduler.TaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class BukkitSchedulerAdapter implements SchedulerService {

    private final Plugin plugin;
    private final TaskScheduler global;
    private final AsyncTaskScheduler async;
    private final SchedulerDiagnostics diagnostics;

    public BukkitSchedulerAdapter(Plugin plugin, boolean foliaDetected) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.global = new BukkitTaskScheduler(false);
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

        private BukkitTaskScheduler(boolean asynchronous) {
            this.asynchronous = asynchronous;
        }

        @Override
        public TaskHandle run(Runnable task) {
            SimpleTaskHandle handle = new SimpleTaskHandle(false);
            BukkitTask scheduled = asynchronous
                ? Bukkit.getScheduler().runTaskAsynchronously(plugin, wrap(handle, task, false))
                : Bukkit.getScheduler().runTask(plugin, wrap(handle, task, false));
            handle.setTaskId(scheduled.getTaskId());
            handle.setCancelAction(scheduled::cancel);
            return handle;
        }

        @Override
        public TaskHandle runLater(Runnable task, long delayTicks) {
            SimpleTaskHandle handle = new SimpleTaskHandle(false);
            BukkitTask scheduled = asynchronous
                ? Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, wrap(handle, task, false), delayTicks)
                : Bukkit.getScheduler().runTaskLater(plugin, wrap(handle, task, false), delayTicks);
            handle.setTaskId(scheduled.getTaskId());
            handle.setCancelAction(scheduled::cancel);
            return handle;
        }

        @Override
        public TaskHandle runRepeating(Runnable task, long delayTicks, long periodTicks) {
            SimpleTaskHandle handle = new SimpleTaskHandle(true);
            BukkitTask scheduled = asynchronous
                ? Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, wrap(handle, task, true), delayTicks, periodTicks)
                : Bukkit.getScheduler().runTaskTimer(plugin, wrap(handle, task, true), delayTicks, periodTicks);
            handle.setTaskId(scheduled.getTaskId());
            handle.setCancelAction(() -> {
                scheduled.cancel();
                handle.markCancelled();
            });
            return handle;
        }

        private Runnable wrap(SimpleTaskHandle handle, Runnable task, boolean repeating) {
            return () -> {
                try {
                    task.run();
                    if (!repeating) {
                        handle.markSuccess();
                    }
                } catch (Throwable throwable) {
                    handle.markFailure(throwable);
                    handle.cancel();
                }
            };
        }
    }

    private final class BukkitAsyncTaskScheduler extends BukkitTaskScheduler implements AsyncTaskScheduler {

        private BukkitAsyncTaskScheduler() {
            super(true);
        }

        @Override
        public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
            CompletableFuture<T> future = new CompletableFuture<>();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    future.complete(supplier.get());
                } catch (Throwable throwable) {
                    future.completeExceptionally(throwable);
                }
            });
            return future;
        }
    }
}
