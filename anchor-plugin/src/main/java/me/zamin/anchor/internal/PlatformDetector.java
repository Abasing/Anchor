package me.zamin.anchor.internal;

import me.zamin.anchor.api.AnchorPlatform;
import org.bukkit.Bukkit;

public final class PlatformDetector {

    private PlatformDetector() {
    }

    public static AnchorPlatform detect() {
        return new AnchorPlatform(
            Bukkit.getName(),
            Bukkit.getVersion(),
            Bukkit.getBukkitVersion(),
            Bukkit.getMinecraftVersion(),
            System.getProperty("java.version", "unknown"),
            classPresent("com.destroystokyo.paper.PaperConfig") || classPresent("io.papermc.paper.threadedregions.RegionizedServer"),
            classPresent("io.papermc.paper.threadedregions.RegionizedServer")
        );
    }

    private static boolean classPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
