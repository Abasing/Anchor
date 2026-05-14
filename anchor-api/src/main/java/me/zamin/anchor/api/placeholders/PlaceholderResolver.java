package me.zamin.anchor.api.placeholders;

import org.bukkit.OfflinePlayer;

/**
 * Resolves a custom placeholder value for an offline or online player.
 */
@FunctionalInterface
public interface PlaceholderResolver {

    /**
     * Resolves placeholder text.
     *
     * @param player player context, may be null depending on caller usage
     * @return resolved text, or null to behave like an empty replacement
     */
    String resolve(OfflinePlayer player);
}
