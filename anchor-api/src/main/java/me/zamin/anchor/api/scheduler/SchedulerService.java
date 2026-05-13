package me.zamin.anchor.api.scheduler;

import me.zamin.anchor.api.AnchorService;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface SchedulerService extends AnchorService {

    TaskHandle runGlobal(Runnable task);

    TaskHandle runAsync(Runnable task);

    TaskHandle runLaterGlobal(Runnable task, long delayTicks);

    TaskHandle runRepeatingGlobal(Runnable task, long delayTicks, long periodTicks);

    TaskHandle runAtLocation(Location location, Runnable task);

    TaskHandle runForEntity(Entity entity, Runnable task);

    void cancel(TaskHandle handle);
}
