package me.zamin.anchor.api;

import java.util.Objects;

/**
 * Static entrypoint used by plugins that consume Anchor at runtime.
 * <p>
 * This class is safe to call from Paper and Folia once the Anchor plugin has
 * completed startup. Consumers should treat the returned {@link AnchorApi} as
 * the long-term stable integration surface.
 */
public final class Anchor {

    private static volatile AnchorApi api;

    private Anchor() {
    }

    /**
     * Returns the active Anchor API implementation.
     *
     * @return non-null active API implementation
     * @throws IllegalStateException if the Anchor plugin has not finished startup
     */
    public static AnchorApi api() {
        AnchorApi current = api;
        if (current == null) {
            throw new IllegalStateException("Anchor API is not available yet.");
        }
        return current;
    }

    /**
     * Returns whether Anchor has bound a runtime API instance.
     *
     * @return {@code true} once Anchor startup completed successfully
     */
    public static boolean isAvailable() {
        return api != null;
    }

    /**
     * Binds the runtime API instance.
     * <p>
     * This method is for Anchor runtime use only and should not be called by
     * dependent plugins.
     *
     * @param anchorApi non-null API implementation
     */
    public static void bind(AnchorApi anchorApi) {
        api = Objects.requireNonNull(anchorApi, "anchorApi");
    }

    /**
     * Clears the runtime API binding during shutdown.
     */
    public static void clear() {
        api = null;
    }
}
