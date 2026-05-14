package me.zamin.anchor.api.scheduler;

import me.zamin.anchor.api.AnchorService;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Platform-neutral scheduling facade. The public API does not expose Folia classes.
 * <p>
 * Consumers should choose a scheduler context based on data ownership rather
 * than assuming a universal server thread.
 */
public interface SchedulerService extends AnchorService {

    /**
     * Returns the global scheduler context.
     * <p>
     * Use this for work tied to global server state such as console-owned
     * actions. Safe on Paper and Folia.
     *
     * @return non-null global scheduler
     */
    TaskScheduler global();

    /**
     * Returns the async scheduler context.
     * <p>
     * Use this for non-Bukkit CPU or I/O work that is safe away from world
     * state. Callbacks may complete off-thread.
     *
     * @return non-null async scheduler
     */
    AsyncTaskScheduler async();

    /**
     * Returns the scheduler context for work owned by a location or region.
     * <p>
     * On Folia this maps to the region scheduler. On Paper and Bukkit it
     * degrades to global scheduling.
     *
     * @param location non-null location
     * @return non-null region scheduler
     */
    TaskScheduler region(Location location);

    /**
     * Returns the scheduler context for work owned by an entity.
     * <p>
     * On Folia this maps to the entity scheduler. On Paper and Bukkit it
     * degrades to global scheduling.
     *
     * @param entity non-null entity
     * @return non-null entity scheduler
     */
    TaskScheduler entity(Entity entity);

    /**
     * Returns the selected scheduler platform.
     *
     * @return non-null scheduler platform
     */
    SchedulerPlatform platform();

    /**
     * Returns diagnostics and safety notes for the active scheduler adapter.
     *
     * @return non-null scheduler diagnostics
     */
    SchedulerDiagnostics diagnostics();

    /**
     * Cancels a task handle returned by Anchor.
     *
     * @param handle task handle, may be null
     */
    void cancel(TaskHandle handle);
}
