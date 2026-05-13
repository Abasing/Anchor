package me.zamin.anchor.api.scheduler;

import me.zamin.anchor.api.AnchorService;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Platform-neutral scheduling facade. The public API does not expose Folia classes.
 */
public interface SchedulerService extends AnchorService {

    TaskScheduler global();

    AsyncTaskScheduler async();

    TaskScheduler region(Location location);

    TaskScheduler entity(Entity entity);

    SchedulerPlatform platform();

    SchedulerDiagnostics diagnostics();

    void cancel(TaskHandle handle);
}
