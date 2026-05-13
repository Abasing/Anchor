package dev.anchor.internal.adapters;

import dev.anchor.adapters.Adapter;
import dev.anchor.adapters.AdapterStatus;
import dev.anchor.core.AnchorProvider;
import dev.anchor.core.AnchorService;
import dev.anchor.core.ProviderPriority;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public abstract class AbstractProviderAdapter<T extends AnchorProvider> implements Adapter {

    private final String name;
    private final String dependency;
    private final ProviderPriority priority;
    private final Class<? extends AnchorService> serviceType;
    private final Plugin plugin;
    private AdapterStatus status = AdapterStatus.PENDING;
    private T provider;

    protected AbstractProviderAdapter(String name, String dependency, ProviderPriority priority,
                                      Class<? extends AnchorService> serviceType, Plugin plugin) {
        this.name = Objects.requireNonNull(name, "name");
        this.dependency = Objects.requireNonNull(dependency, "dependency");
        this.priority = Objects.requireNonNull(priority, "priority");
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    public String getModuleName() {
        return name;
    }

    @Override
    public void enable() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        if (!pluginManager.isPluginEnabled(dependency)) {
            status = AdapterStatus.SKIPPED;
            return;
        }
        provider = createProvider();
        status = provider == null ? AdapterStatus.SKIPPED : AdapterStatus.LOADED;
    }

    @Override
    public void disable() {
        provider = null;
        if (status == AdapterStatus.LOADED) {
            status = AdapterStatus.SKIPPED;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPluginDependencyName() {
        return dependency;
    }

    @Override
    public ProviderPriority getPriority() {
        return priority;
    }

    @Override
    public boolean isLoaded() {
        return provider != null;
    }

    @Override
    public AdapterStatus getStatus() {
        return status;
    }

    @Override
    public Optional<Class<? extends AnchorService>> getServiceType() {
        return Optional.of(serviceType);
    }

    @Override
    public Optional<AnchorProvider> getProvider() {
        return Optional.ofNullable(provider);
    }

    protected abstract T createProvider();

    protected Plugin plugin() {
        return plugin;
    }

    public void markFailed() {
        status = AdapterStatus.FAILED;
        provider = null;
    }
}
