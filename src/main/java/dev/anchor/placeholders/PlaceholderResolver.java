package dev.anchor.placeholders;

import org.bukkit.OfflinePlayer;

@FunctionalInterface
public interface PlaceholderResolver {

    String resolve(OfflinePlayer player);
}
