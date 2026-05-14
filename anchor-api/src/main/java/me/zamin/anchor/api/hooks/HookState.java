package me.zamin.anchor.api.hooks;

/**
 * Lifecycle state for a detected integration hook.
 */
public enum HookState {
    ACTIVE,
    FALLBACK,
    MISSING,
    DISABLED,
    FAILED,
    SKELETON
}
