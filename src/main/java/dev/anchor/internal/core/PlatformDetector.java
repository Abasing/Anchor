package dev.anchor.internal.core;

import dev.anchor.core.AnchorPlatform;
import org.bukkit.Bukkit;

public final class PlatformDetector {

    public AnchorPlatform detect() {
        String serverName = Bukkit.getName();
        String serverVersion = Bukkit.getVersion();
        String bukkitVersion = Bukkit.getBukkitVersion();
        String minecraftVersion = Bukkit.getMinecraftVersion();
        String javaVersion = System.getProperty("java.version", "unknown");
        boolean paper = classPresent("com.destroystokyo.paper.PaperConfig") || classPresent("io.papermc.paper.threadedregions.RegionizedServer");
        boolean folia = classPresent("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
        return new AnchorPlatform(serverName, serverVersion, bukkitVersion, minecraftVersion, javaVersion, paper, folia);
    }

    private boolean classPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
