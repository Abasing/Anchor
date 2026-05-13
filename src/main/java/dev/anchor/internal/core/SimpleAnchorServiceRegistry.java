package dev.anchor.internal.core;

import dev.anchor.core.AnchorService;
import dev.anchor.core.AnchorServiceRegistry;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SimpleAnchorServiceRegistry implements AnchorServiceRegistry {

    private final Map<Class<? extends AnchorService>, AnchorService> services = new ConcurrentHashMap<>();

    @Override
    public <T extends AnchorService> void register(Class<T> type, T service) {
        services.put(type, service);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends AnchorService> Optional<T> resolve(Class<T> type) {
        return Optional.ofNullable((T) services.get(type));
    }

    @Override
    public Collection<AnchorService> services() {
        return services.values();
    }
}
