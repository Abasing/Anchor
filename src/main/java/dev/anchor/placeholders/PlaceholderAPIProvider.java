package dev.anchor.placeholders;

import dev.anchor.core.ProviderPriority;
import dev.anchor.core.ServiceStatus;
import java.util.Objects;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class PlaceholderAPIProvider implements PlaceholderProvider {

    private final InternalPlaceholderProvider delegate;

    public PlaceholderAPIProvider(InternalPlaceholderProvider delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public String parse(Player player, String text) {
        return PlaceholderAPI.setPlaceholders(player, delegate.parse(player, text));
    }

    @Override
    public String parse(OfflinePlayer player, String text) {
        return PlaceholderAPI.setPlaceholders(player, delegate.parse(player, text));
    }

    @Override
    public void register(String identifier, PlaceholderResolver resolver) {
        delegate.register(identifier, resolver);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getProviderName() {
        return "PlaceholderAPI";
    }

    @Override
    public ServiceStatus getStatus() {
        return ServiceStatus.AVAILABLE;
    }

    @Override
    public ProviderPriority getPriority() {
        return ProviderPriority.HIGH;
    }
}
