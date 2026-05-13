package me.zamin.anchor.api.services;

import java.util.Collection;
import java.util.Optional;
import me.zamin.anchor.api.AnchorService;

public interface ServiceRegistry {

    <T extends AnchorService> Optional<T> resolve(Class<T> serviceType);

    Collection<AnchorService> all();
}
