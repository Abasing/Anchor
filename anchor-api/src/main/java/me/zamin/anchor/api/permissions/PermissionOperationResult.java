package me.zamin.anchor.api.permissions;

import java.util.Objects;

/**
 * Result for one permission inside a batch mutation operation.
 *
 * @param permission non-null permission node
 * @param success whether the individual operation succeeded
 * @param supported whether the provider supported the individual operation
 * @param reason human-readable status or failure reason
 */
public record PermissionOperationResult(
    String permission,
    boolean success,
    boolean supported,
    String reason
) {

    public PermissionOperationResult {
        Objects.requireNonNull(permission, "permission");
        Objects.requireNonNull(reason, "reason");
    }

    /**
     * Converts a single-permission result into a batch operation result entry.
     *
     * @param permission non-null permission node
     * @param result non-null single-operation result
     * @return non-null operation result entry
     */
    public static PermissionOperationResult from(String permission, PermissionResult result) {
        Objects.requireNonNull(result, "result");
        return new PermissionOperationResult(permission, result.success(), result.supported(), result.reason());
    }
}
