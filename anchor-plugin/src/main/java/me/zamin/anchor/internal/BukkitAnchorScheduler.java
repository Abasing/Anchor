package me.zamin.anchor.internal;

import me.zamin.anchor.api.ServiceStatus;
import me.zamin.anchor.api.scheduler.SchedulerService;
import me.zamin.anchor.api.scheduler.TaskHandle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class BukkitAnchorScheduler implements SchedulerService {

    private final Plugin plugin;

    public BukkitAnchorScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public TaskHandle runGlobal(Runnable task) {
        return wrap(Bukkit.getScheduler().runTask(plugin, task));
    }

    @Override
    public TaskHandle runAsync(Runnable task) {
        return wrap(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    @Override
    public TaskHandle runLaterGlobal(Runnable task, long delayTicks) {
        return wrap(Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
    }

    @Override
    public TaskHandle runRepeatingGlobal(Runnable task, long delayTicks, long periodTicks) {
        return wrap(Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks));
    }

    @Override
    public TaskHandle runAtLocation(Location location, Runnable task) {
        return runGlobal(task);
    }

    @Override
    public TaskHandle runForEntity(Entity entity, Runnable task) {
        return runGlobal(task);
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
        return "BukkitScheduler";
    }

    @Override
    public ServiceStatus status() {
        return ServiceStatus.AVAILABLE;
    }

    private TaskHandle wrap(BukkitTask task) {
        return new TaskHandle() {
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
        };
    }
}
