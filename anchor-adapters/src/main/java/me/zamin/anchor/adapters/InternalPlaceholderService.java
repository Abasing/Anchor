package me.zamin.anchor.adapters;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import me.zamin.anchor.api.ServiceStatus;
import me.zamin.anchor.api.placeholders.PlaceholderResolver;
import me.zamin.anchor.api.placeholders.PlaceholderService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class InternalPlaceholderService implements PlaceholderService {

    private final Map<String, PlaceholderResolver> resolvers = new ConcurrentHashMap<>();

    public InternalPlaceholderService(String serverVersion) {
        register("player", player -> player != null && player.getName() != null ? player.getName() : "unknown");
        register("uuid", player -> player != null ? player.getUniqueId().toString() : "unknown");
        register("world", player -> player != null && player.isOnline() && player.getPlayer() != null
            ? player.getPlayer().getWorld().getName() : "unknown");
        register("online", player -> Integer.toString(Bukkit.getOnlinePlayers().size()));
        register("server_version", player -> serverVersion);
    }

    @Override
    public String parse(Player player, String text) {
        return parse((OfflinePlayer) player, text);
    }

    @Override
    public String parse(OfflinePlayer player, String text) {
        String parsed = Objects.requireNonNull(text, "text");
        for (Map.Entry<String, PlaceholderResolver> entry : resolvers.entrySet()) {
            parsed = parsed.replace("{" + entry.getKey() + "}", Objects.toString(entry.getValue().resolve(player), ""));
        }
        return parsed;
    }

    @Override
    public void register(String identifier, PlaceholderResolver resolver) {
        resolvers.put(identifier.toLowerCase(), resolver);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String providerName() {
        return "Internal";
    }

    @Override
    public ServiceStatus status() {
        return ServiceStatus.FALLBACK;
    }
}
