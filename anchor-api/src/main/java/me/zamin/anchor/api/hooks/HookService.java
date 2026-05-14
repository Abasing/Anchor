package me.zamin.anchor.api.hooks;

import java.util.Collection;
import java.util.Optional;
import me.zamin.anchor.api.AnchorService;

/**
 * Read-only hook and adapter status service.
 */
public interface HookService extends AnchorService {

    /**
     * Returns all known hooks.
     *
     * @return non-null hook status collection
     */
    Collection<HookStatus> all();

    /**
     * Finds a hook by name.
     *
     * @param hookName non-null hook name
     * @return optional hook status
     */
    Optional<HookStatus> find(String hookName);
}
