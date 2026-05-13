package dev.anchor.core;

import java.util.Collection;
import java.util.Optional;

public interface AnchorServiceRegistry {

    <T extends AnchorService> void register(Class<T> type, T service);

    <T extends AnchorService> Optional<T> resolve(Class<T> type);

    Collection<AnchorService> services();
}
