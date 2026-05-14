package me.zamin.anchor.api.hooks;

/**
 * Snapshot of a single hook or adapter state.
 *
 * @param hookName human-readable hook name
 * @param dependencyName dependency plugin or system name
 * @param state hook lifecycle state
 * @param providerName active provider or fallback name
 * @param message human-readable summary
 * @param loadMillis load or detection time in milliseconds
 */
public record HookStatus(
    String hookName,
    String dependencyName,
    HookState state,
    String providerName,
    String message,
    long loadMillis
) {
}
