package dev.anchor.adapters;

import dev.anchor.core.AnchorModule;
import dev.anchor.core.AnchorProvider;
import dev.anchor.core.ProviderPriority;
import java.util.Optional;

public interface Adapter extends AnchorModule {

    String getName();

    String getPluginDependencyName();

    ProviderPriority getPriority();

    boolean isLoaded();

    AdapterStatus getStatus();

    Optional<Class<? extends dev.anchor.core.AnchorService>> getServiceType();

    Optional<AnchorProvider> getProvider();
}
