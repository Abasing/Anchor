package dev.anchor;

import dev.anchor.commands.AnchorCommand;
import dev.anchor.core.AnchorPlatform;
import dev.anchor.internal.core.AnchorBootstrap;
import dev.anchor.internal.core.PlatformDetector;
import dev.anchor.internal.core.SimpleAnchorLogger;
import java.util.Objects;
import org.bukkit.command.PluginCommand;

public final class AnchorPlugin extends dev.anchor.core.AnchorPlugin {

    private AnchorBootstrap bootstrap;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        bootstrap = createBootstrap();
        bootstrap.enable();
        Anchor.bind(this, bootstrap.api());
        registerCommands();
        bootstrap.logger().info("Anchor enabled on " + bootstrap.platform().serverName() + " " + bootstrap.platform().minecraftVersion());
    }

    @Override
    public void onDisable() {
        Anchor.clear();
        if (bootstrap != null) {
            bootstrap.disable();
        }
    }

    public void reloadAnchor() {
        reloadConfig();
        if (bootstrap != null) {
            bootstrap.disable();
        }
        bootstrap = createBootstrap();
        bootstrap.enable();
        Anchor.bind(this, bootstrap.api());
    }

    public AnchorBootstrap bootstrap() {
        return Objects.requireNonNull(bootstrap, "bootstrap");
    }

    private AnchorBootstrap createBootstrap() {
        boolean debug = getConfig().getBoolean("debug", false);
        SimpleAnchorLogger logger = new SimpleAnchorLogger(getLogger(), debug);
        AnchorPlatform platform = new PlatformDetector().detect();
        return new AnchorBootstrap(this, logger, platform);
    }

    private void registerCommands() {
        PluginCommand command = getCommand("anchor");
        if (command == null) {
            throw new IllegalStateException("Anchor command is missing from plugin.yml");
        }
        AnchorCommand executor = new AnchorCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }
}
