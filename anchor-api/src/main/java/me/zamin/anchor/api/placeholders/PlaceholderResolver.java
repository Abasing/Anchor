package me.zamin.anchor.api.placeholders;

import org.bukkit.OfflinePlayer;

@FunctionalInterface
public interface PlaceholderResolver {

    String resolve(OfflinePlayer player);
}
