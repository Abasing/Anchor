package me.zamin.anchor.examples.foliasafe;

import me.zamin.anchor.api.Anchor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public final class FoliaSafeTasksPlugin extends JavaPlugin {

    public void runForLocation(Location location) {
        Anchor.api().scheduler().region(location).run(() -> location.getBlock().getState().update());
    }

    public void runForEntity(Entity entity) {
        Anchor.api().scheduler().entity(entity).run(() -> entity.setGlowing(true));
    }
}
