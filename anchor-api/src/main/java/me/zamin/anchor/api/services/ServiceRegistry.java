package me.zamin.anchor.api.services;

import java.util.Collection;
import java.util.Optional;
import me.zamin.anchor.api.AnchorService;

/**
 * Read-only registry of public Anchor services.
 */
public interface ServiceRegistry {

    /**
     * Resolves a public service by type.
     *
     * @param serviceType non-null service type
     * @param <T> public service type
     * @return optional resolved service
     */
    <T extends AnchorService> Optional<T> resolve(Class<T> serviceType);

    /**
     * Returns all registered public services.
     *
     * @return non-null service collection
     */
    Collection<AnchorService> all();
}
