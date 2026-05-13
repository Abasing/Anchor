package me.zamin.anchor.adapters;

import me.clip.placeholderapi.PlaceholderAPI;
import me.zamin.anchor.api.ServiceStatus;
import me.zamin.anchor.api.placeholders.PlaceholderResolver;
import me.zamin.anchor.api.placeholders.PlaceholderService;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class PlaceholderApiService implements PlaceholderService {

    private final InternalPlaceholderService delegate;

    public PlaceholderApiService(InternalPlaceholderService delegate) {
        this.delegate = delegate;
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
    public String providerName() {
        return "PlaceholderAPI";
    }

    @Override
    public ServiceStatus status() {
        return ServiceStatus.AVAILABLE;
    }
}
