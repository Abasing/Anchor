package dev.anchor;

import dev.anchor.core.AnchorAPI;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

public final class Anchor {

    private static volatile AnchorAPI api;
    private static volatile JavaPlugin plugin;

    private Anchor() {
    }

    public static AnchorAPI api() {
        AnchorAPI current = api;
        if (current == null) {
            throw new IllegalStateException("Anchor API is not available yet.");
        }
        return current;
    }

    public static boolean isAvailable() {
        return api != null;
    }

    public static JavaPlugin plugin() {
        return Objects.requireNonNull(plugin, "Anchor plugin is not available yet.");
    }

    public static void bind(JavaPlugin javaPlugin, AnchorAPI anchorApi) {
        plugin = Objects.requireNonNull(javaPlugin, "javaPlugin");
        api = Objects.requireNonNull(anchorApi, "anchorApi");
    }

    public static void clear() {
        api = null;
        plugin = null;
    }
}
