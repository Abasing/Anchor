package dev.anchor.placeholders;

import dev.anchor.core.ProviderPriority;
import dev.anchor.core.ServiceStatus;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class InternalPlaceholderProvider implements PlaceholderProvider {

    private final Map<String, PlaceholderResolver> resolvers = new ConcurrentHashMap<>();

    public InternalPlaceholderProvider(String serverVersion) {
        register("player", player -> player != null && player.getName() != null ? player.getName() : "unknown");
        register("uuid", player -> player != null && player.getUniqueId() != null ? player.getUniqueId().toString() : "unknown");
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
        Objects.requireNonNull(text, "text");
        String parsed = text;
        for (Map.Entry<String, PlaceholderResolver> entry : resolvers.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            if (parsed.contains(placeholder)) {
                String replacement = entry.getValue().resolve(player);
                parsed = parsed.replace(placeholder, replacement == null ? "" : replacement);
            }
        }
        return parsed;
    }

    @Override
    public void register(String identifier, PlaceholderResolver resolver) {
        Objects.requireNonNull(identifier, "identifier");
        Objects.requireNonNull(resolver, "resolver");
        resolvers.put(identifier.toLowerCase(), resolver);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getProviderName() {
        return "Internal";
    }

    @Override
    public ServiceStatus getStatus() {
        return ServiceStatus.FALLBACK;
    }

    @Override
    public ProviderPriority getPriority() {
        return ProviderPriority.LOW;
    }
}
