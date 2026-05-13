package me.zamin.anchor.api;

import java.util.Objects;

public final class Anchor {

    private static volatile AnchorApi api;

    private Anchor() {
    }

    public static AnchorApi api() {
        AnchorApi current = api;
        if (current == null) {
            throw new IllegalStateException("Anchor API is not available yet.");
        }
        return current;
    }

    public static boolean isAvailable() {
        return api != null;
    }

    public static void bind(AnchorApi anchorApi) {
        api = Objects.requireNonNull(anchorApi, "anchorApi");
    }

    public static void clear() {
        api = null;
    }
}
