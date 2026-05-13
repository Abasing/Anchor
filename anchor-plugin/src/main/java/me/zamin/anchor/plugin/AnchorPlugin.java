package me.zamin.anchor.plugin;

import me.zamin.anchor.api.Anchor;
import me.zamin.anchor.internal.AnchorRuntime;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class AnchorPlugin extends JavaPlugin {

    private AnchorRuntime runtime;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        runtime = new AnchorRuntime(this);
        runtime.enable();
        Anchor.bind(runtime.api());
        PluginCommand command = getCommand("anchor");
        if (command == null) {
            throw new IllegalStateException("Anchor command is missing from plugin.yml");
        }
        AnchorCommand executor = new AnchorCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    @Override
    public void onDisable() {
        Anchor.clear();
        if (runtime != null) {
            runtime.disable();
        }
    }

    public void reloadAnchor() {
        reloadConfig();
        runtime.disable();
        runtime = new AnchorRuntime(this);
        runtime.enable();
        Anchor.bind(runtime.api());
    }

    public AnchorRuntime runtime() {
        return runtime;
    }
}
