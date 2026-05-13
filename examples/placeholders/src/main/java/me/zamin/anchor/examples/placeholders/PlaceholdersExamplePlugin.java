package me.zamin.anchor.examples.placeholders;

import me.zamin.anchor.api.Anchor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlaceholdersExamplePlugin extends JavaPlugin {

    public String render(Player player) {
        return Anchor.api().placeholders().parse(player, "Hello {player}");
    }
}
