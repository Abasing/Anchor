package me.zamin.anchor.internal;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import me.zamin.anchor.api.ServiceStatus;
import me.zamin.anchor.api.scheduler.AsyncTaskScheduler;
import me.zamin.anchor.api.scheduler.SchedulerDiagnostics;
import me.zamin.anchor.api.scheduler.SchedulerPlatform;
import me.zamin.anchor.api.scheduler.SchedulerService;
import me.zamin.anchor.api.scheduler.TaskHandle;
import me.zamin.anchor.api.scheduler.TaskScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public final class FoliaSchedulerAdapter implements SchedulerService {

    private final Plugin plugin;
    private final GlobalTaskScheduler global;
    private final FoliaAsyncTaskScheduler async;
    private final SchedulerDiagnostics diagnostics;

    public FoliaSchedulerAdapter(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.global = new GlobalTaskScheduler(plugin.getServer().getGlobalRegionScheduler());
        this.async = new FoliaAsyncTaskScheduler(plugin.getServer().getAsyncScheduler());
        this.diagnostics = new SchedulerDiagnostics(
            SchedulerPlatform.FOLIA,
            true,
            true,
            true,
            true,
            true,
            "FoliaSchedulerAdapter",
            List.of(
                "Use region(location) for location-owned operations.",
                "Use entity(entity) for entity-owned operations so tasks follow the entity across regions."
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
        return new RegionTaskScheduler(plugin.getServer().getRegionScheduler(), location);
    }

    @Override
    public TaskScheduler entity(Entity entity) {
        return new EntityTaskScheduler(entity.getScheduler(), entity);
    }

    @Override
    public SchedulerPlatform platform() {
        return SchedulerPlatform.FOLIA;
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
        return "FoliaSchedulerAdapter";
    }

    @Override
    public ServiceStatus status() {
        return ServiceStatus.AVAILABLE;
    }

    private abstract class AbstractFoliaTaskScheduler implements TaskScheduler {

        protected Runnable wrap(SimpleTaskHandle handle, Runnable task, boolean repeating) {
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

        protected ConsumerTask consumer(SimpleTaskHandle handle, Runnable task, boolean repeating) {
            return scheduledTask -> wrap(handle, task, repeating).run();
        }
    }

    @FunctionalInterface
    private interface ConsumerTask extends java.util.function.Consumer<ScheduledTask> {
    }

    private final class GlobalTaskScheduler extends AbstractFoliaTaskScheduler {

        private final GlobalRegionScheduler scheduler;

        private GlobalTaskScheduler(GlobalRegionScheduler scheduler) {
            this.scheduler = scheduler;
        }

        @Override
        public TaskHandle run(Runnable task) {
            SimpleTaskHandle handle = new SimpleTaskHandle(false);
            ScheduledTask scheduled = scheduler.run(plugin, consumer(handle, task, false));
            bind(handle, scheduled);
            return handle;
        }

        @Override
        public TaskHandle runLater(Runnable task, long delayTicks) {
            SimpleTaskHandle handle = new SimpleTaskHandle(false);
            ScheduledTask scheduled = scheduler.runDelayed(plugin, consumer(handle, task, false), delayTicks);
            bind(handle, scheduled);
            return handle;
        }

        @Override
        public TaskHandle runRepeating(Runnable task, long delayTicks, long periodTicks) {
            SimpleTaskHandle handle = new SimpleTaskHandle(true);
            ScheduledTask scheduled = scheduler.runAtFixedRate(plugin, consumer(handle, task, true), delayTicks, periodTicks);
            bind(handle, scheduled);
            return handle;
        }
    }

    private final class RegionTaskScheduler extends AbstractFoliaTaskScheduler {

        private final RegionScheduler scheduler;
        private final Location location;

        private RegionTaskScheduler(RegionScheduler scheduler, Location location) {
            this.scheduler = scheduler;
            this.location = Objects.requireNonNull(location, "location");
        }

        @Override
        public TaskHandle run(Runnable task) {
            SimpleTaskHandle handle = new SimpleTaskHandle(false);
            ScheduledTask scheduled = scheduler.run(plugin, location, consumer(handle, task, false));
            bind(handle, scheduled);
            return handle;
        }

        @Override
        public TaskHandle runLater(Runnable task, long delayTicks) {
            SimpleTaskHandle handle = new SimpleTaskHandle(false);
            ScheduledTask scheduled = scheduler.runDelayed(plugin, location, consumer(handle, task, false), delayTicks);
            bind(handle, scheduled);
            return handle;
        }

        @Override
        public TaskHandle runRepeating(Runnable task, long delayTicks, long periodTicks) {
            SimpleTaskHandle handle = new SimpleTaskHandle(true);
            ScheduledTask scheduled = scheduler.runAtFixedRate(plugin, location, consumer(handle, task, true), delayTicks, periodTicks);
            bind(handle, scheduled);
            return handle;
        }
    }

    private final class EntityTaskScheduler extends AbstractFoliaTaskScheduler {

        private final EntityScheduler scheduler;
        private final Entity entity;

        private EntityTaskScheduler(EntityScheduler scheduler, Entity entity) {
            this.scheduler = scheduler;
            this.entity = entity;
        }

        @Override
        public TaskHandle run(Runnable task) {
            SimpleTaskHandle handle = new SimpleTaskHandle(false);
            ScheduledTask scheduled = scheduler.run(plugin, consumer(handle, task, false), () -> handle.markCancelled());
            bind(handle, scheduled);
            return handle;
        }

        @Override
        public TaskHandle runLater(Runnable task, long delayTicks) {
            SimpleTaskHandle handle = new SimpleTaskHandle(false);
            ScheduledTask scheduled = scheduler.runDelayed(plugin, consumer(handle, task, false), () -> handle.markCancelled(), delayTicks);
            bind(handle, scheduled);
            return handle;
        }

        @Override
        public TaskHandle runRepeating(Runnable task, long delayTicks, long periodTicks) {
            SimpleTaskHandle handle = new SimpleTaskHandle(true);
            ScheduledTask scheduled = scheduler.runAtFixedRate(plugin, consumer(handle, task, true), () -> handle.markCancelled(), delayTicks, periodTicks);
            bind(handle, scheduled);
            return handle;
        }
    }

    private final class FoliaAsyncTaskScheduler extends AbstractFoliaTaskScheduler implements AsyncTaskScheduler {

        private final AsyncScheduler scheduler;

        private FoliaAsyncTaskScheduler(AsyncScheduler scheduler) {
            this.scheduler = scheduler;
        }

        @Override
        public TaskHandle run(Runnable task) {
            SimpleTaskHandle handle = new SimpleTaskHandle(false);
            ScheduledTask scheduled = scheduler.runNow(plugin, consumer(handle, task, false));
            bind(handle, scheduled);
            return handle;
        }

        @Override
        public TaskHandle runLater(Runnable task, long delayTicks) {
            SimpleTaskHandle handle = new SimpleTaskHandle(false);
            ScheduledTask scheduled = scheduler.runDelayed(plugin, consumer(handle, task, false), delayTicks * 50L, TimeUnit.MILLISECONDS);
            bind(handle, scheduled);
            return handle;
        }

        @Override
        public TaskHandle runRepeating(Runnable task, long delayTicks, long periodTicks) {
            SimpleTaskHandle handle = new SimpleTaskHandle(true);
            ScheduledTask scheduled = scheduler.runAtFixedRate(plugin, consumer(handle, task, true), delayTicks * 50L, periodTicks * 50L, TimeUnit.MILLISECONDS);
            bind(handle, scheduled);
            return handle;
        }

        @Override
        public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
            CompletableFuture<T> future = new CompletableFuture<>();
            scheduler.runNow(plugin, task -> {
                try {
                    future.complete(supplier.get());
                } catch (Throwable throwable) {
                    future.completeExceptionally(throwable);
                }
            });
            return future;
        }
    }

    private void bind(SimpleTaskHandle handle, ScheduledTask scheduledTask) {
        handle.setTaskId(System.identityHashCode(scheduledTask));
        handle.setCancelAction(() -> {
            scheduledTask.cancel();
            handle.markCancelled();
        });
    }
}
