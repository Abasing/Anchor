package me.zamin.anchor.api.placeholders;

import me.zamin.anchor.api.AnchorService;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Stable abstraction over PlaceholderAPI with internal fallback placeholder
 * support.
 */
public interface PlaceholderService extends AnchorService {

    /**
     * Parses text for a live player context.
     *
     * @param player player context, may be null if the implementation supports it
     * @param text non-null input text
     * @return non-null parsed text
     */
    String parse(Player player, String text);

    /**
     * Parses text for an offline player context.
     *
     * @param player offline player context, may be null if the implementation supports it
     * @param text non-null input text
     * @return non-null parsed text
     */
    String parse(OfflinePlayer player, String text);

    /**
     * Registers an internal custom placeholder resolver.
     * <p>
     * Implementations should keep this available even when PlaceholderAPI is
     * missing so dependent plugins can rely on internal placeholder support.
     *
     * @param identifier non-null placeholder identifier without braces
     * @param resolver non-null placeholder resolver
     */
    void register(String identifier, PlaceholderResolver resolver);
}
