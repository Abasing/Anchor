package me.zamin.anchor.examples.schedulerdemo;

import me.zamin.anchor.api.Anchor;
import org.bukkit.plugin.java.JavaPlugin;

public final class SchedulerDemoPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Anchor.api().scheduler().global().runLater(() -> getLogger().info("Global task through Anchor"), 20L);
        Anchor.api().scheduler().async().supplyAsync(() -> "async-result").thenAccept(getLogger()::info);
    }
}
