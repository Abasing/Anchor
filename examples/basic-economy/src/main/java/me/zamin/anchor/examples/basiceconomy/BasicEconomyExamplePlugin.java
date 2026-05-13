package me.zamin.anchor.examples.basiceconomy;

import me.zamin.anchor.api.Anchor;
import org.bukkit.plugin.java.JavaPlugin;

public final class BasicEconomyExamplePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Economy provider: " + Anchor.api().economy().providerName());
    }
}
