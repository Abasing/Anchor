package dev.anchor.placeholders;

import dev.anchor.core.AnchorService;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface PlaceholderService extends AnchorService {

    String parse(Player player, String text);

    String parse(OfflinePlayer player, String text);

    void register(String identifier, PlaceholderResolver resolver);
}
