package me.zamin.anchor.examples.regions;

import me.zamin.anchor.api.Anchor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class RegionsExamplePlugin extends JavaPlugin {

    public boolean canBuild(Player player, Location location) {
        return Anchor.api().regions().canBuild(player, location);
    }
}
