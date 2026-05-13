package me.zamin.anchor.api.hooks;

public record HookStatus(
    String hookName,
    String dependencyName,
    HookState state,
    String providerName,
    String message
) {
}
