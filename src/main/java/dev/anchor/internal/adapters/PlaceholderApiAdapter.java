package dev.anchor.internal.adapters;

import dev.anchor.core.ProviderPriority;
import dev.anchor.placeholders.InternalPlaceholderProvider;
import dev.anchor.placeholders.PlaceholderAPIProvider;
import dev.anchor.placeholders.PlaceholderService;
import org.bukkit.plugin.Plugin;

public final class PlaceholderApiAdapter extends AbstractProviderAdapter<PlaceholderAPIProvider> {

    private final InternalPlaceholderProvider delegate;

    public PlaceholderApiAdapter(Plugin plugin, InternalPlaceholderProvider delegate) {
        super("PlaceholderAPI", "PlaceholderAPI", ProviderPriority.HIGH, PlaceholderService.class, plugin);
        this.delegate = delegate;
    }

    @Override
    protected PlaceholderAPIProvider createProvider() {
        return new PlaceholderAPIProvider(delegate);
    }
}
