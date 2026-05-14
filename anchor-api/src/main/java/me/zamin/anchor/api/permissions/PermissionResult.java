package me.zamin.anchor.api.permissions;

import java.util.Objects;

/**
 * Result for permission mutation operations.
 *
 * @param success whether the operation completed successfully
 * @param supported whether the active provider supports the requested mutation
 * @param reason human-readable reason or status message
 * @param providerName provider that produced the result
 */
public record PermissionResult(
    boolean success,
    boolean supported,
    String reason,
    String providerName
) {

    public PermissionResult {
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(providerName, "providerName");
    }

    /**
     * Creates a successful mutation result.
     *
     * @param providerName non-null provider name
     * @param reason non-null status message
     * @return successful result
     */
    public static PermissionResult success(String providerName, String reason) {
        return new PermissionResult(true, true, reason, providerName);
    }

    /**
     * Creates a supported-but-failed mutation result.
     *
     * @param providerName non-null provider name
     * @param reason non-null failure reason
     * @return failure result
     */
    public static PermissionResult failure(String providerName, String reason) {
        return new PermissionResult(false, true, reason, providerName);
    }

    /**
     * Creates an unsupported mutation result.
     *
     * @param providerName non-null provider name
     * @param reason non-null unsupported reason
     * @return unsupported result
     */
    public static PermissionResult unsupported(String providerName, String reason) {
        return new PermissionResult(false, false, reason, providerName);
    }
}
