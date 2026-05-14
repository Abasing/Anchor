package me.zamin.anchor.api;

/**
 * Base contract implemented by all public Anchor services.
 */
public interface AnchorService {

    /**
     * Returns whether a real backing provider is available.
     * <p>
     * A service may still be callable when this returns {@code false}; callers
     * should inspect {@link #status()} to understand fallback behavior.
     *
     * @return {@code true} when a real provider is active
     */
    boolean isAvailable();

    /**
     * Returns the active provider name or fallback label.
     *
     * @return non-null provider or fallback name
     */
    String providerName();

    /**
     * Returns the current service status.
     *
     * @return non-null status enum
     */
    ServiceStatus status();
}
