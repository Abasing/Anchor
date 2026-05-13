package dev.anchor.scheduler;

import dev.anchor.core.ServiceStatus;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class BukkitSchedulerService implements SchedulerService {

    private final Plugin plugin;

    public BukkitSchedulerService(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    public TaskHandle runSync(Runnable runnable) {
        return wrap(Bukkit.getScheduler().runTask(plugin, runnable));
    }

    @Override
    public TaskHandle runAsync(Runnable runnable) {
        return wrap(Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    @Override
    public TaskHandle runLater(Runnable runnable, long ticks) {
        return wrap(Bukkit.getScheduler().runTaskLater(plugin, runnable, ticks));
    }

    @Override
    public TaskHandle runRepeating(Runnable runnable, long delayTicks, long periodTicks) {
        return wrap(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks));
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
    public String getProviderName() {
        return "BukkitScheduler";
    }

    @Override
    public ServiceStatus getStatus() {
        return ServiceStatus.AVAILABLE;
    }

    private TaskHandle wrap(BukkitTask task) {
        return new BukkitTaskHandle(task);
    }

    private record BukkitTaskHandle(BukkitTask task) implements TaskHandle {
        @Override
        public int taskId() {
            return task.getTaskId();
        }

        @Override
        public void cancel() {
            task.cancel();
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }
    }
}
