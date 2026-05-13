package me.zamin.anchor.internal.adapters.meta;

import java.util.Set;

public record AdapterMetadata(
    String name,
    String dependency,
    AdapterLifecycleState state,
    Set<AdapterCapability> capabilities,
    String providerName,
    String message,
    long loadMillis
) {
}
