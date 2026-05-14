package me.zamin.anchor.api.permissions;

import java.util.List;
import java.util.Objects;

/**
 * Result for a batch permission mutation operation.
 * <p>
 * Batch operations are not database transactions. Partial success is possible,
 * and rollback is best-effort only.
 *
 * @param providerName non-null provider that produced the result
 * @param success whether the overall requested batch completed fully
 * @param supported whether the provider supported the requested batch
 * @param attemptedPermissions non-null permissions that were attempted in
 *                             input order
 * @param successfulPermissions non-null permissions that succeeded during the
 *                              original batch operation before any rollback
 * @param failedPermissions non-null failed permission entries with reasons
 * @param rollbackAttempted whether rollback was attempted after failure
 * @param rollbackSucceeded whether rollback completed without rollback failure
 * @param rolledBackPermissions non-null permissions successfully rolled back
 * @param rollbackFailures non-null rollback failures with reasons
 * @param reason human-readable summary
 */
public record PermissionBatchResult(
    String providerName,
    boolean success,
    boolean supported,
    List<String> attemptedPermissions,
    List<String> successfulPermissions,
    List<PermissionOperationResult> failedPermissions,
    boolean rollbackAttempted,
    boolean rollbackSucceeded,
    List<String> rolledBackPermissions,
    List<PermissionOperationResult> rollbackFailures,
    String reason
) {

    public PermissionBatchResult {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(attemptedPermissions, "attemptedPermissions");
        Objects.requireNonNull(successfulPermissions, "successfulPermissions");
        Objects.requireNonNull(failedPermissions, "failedPermissions");
        Objects.requireNonNull(rolledBackPermissions, "rolledBackPermissions");
        Objects.requireNonNull(rollbackFailures, "rollbackFailures");
        Objects.requireNonNull(reason, "reason");
        attemptedPermissions = List.copyOf(attemptedPermissions);
        successfulPermissions = List.copyOf(successfulPermissions);
        failedPermissions = List.copyOf(failedPermissions);
        rolledBackPermissions = List.copyOf(rolledBackPermissions);
        rollbackFailures = List.copyOf(rollbackFailures);
        supported = supported && failedPermissions.stream().noneMatch(result -> !result.supported())
            && rollbackFailures.stream().noneMatch(result -> !result.supported());
    }

    /**
     * Creates a fully successful batch result.
     *
     * @param providerName non-null provider name
     * @param attemptedPermissions non-null attempted permissions
     * @param successfulPermissions non-null successful permissions
     * @param reason non-null human-readable summary
     * @return successful batch result
     */
    public static PermissionBatchResult success(
        String providerName,
        List<String> attemptedPermissions,
        List<String> successfulPermissions,
        String reason
    ) {
        return new PermissionBatchResult(
            providerName,
            true,
            true,
            attemptedPermissions,
            successfulPermissions,
            List.of(),
            false,
            false,
            List.of(),
            List.of(),
            reason
        );
    }

    /**
     * Creates a failed batch result.
     *
     * @param providerName non-null provider name
     * @param attemptedPermissions non-null attempted permissions
     * @param successfulPermissions non-null successful permissions before
     *                              rollback
     * @param failedPermissions non-null failed permissions with reasons
     * @param rollbackAttempted whether rollback was attempted
     * @param rollbackSucceeded whether rollback completed without rollback failure
     * @param rolledBackPermissions non-null successfully rolled-back permissions
     * @param rollbackFailures non-null rollback failures with reasons
     * @param reason non-null human-readable summary
     * @return failed batch result
     */
    public static PermissionBatchResult failure(
        String providerName,
        List<String> attemptedPermissions,
        List<String> successfulPermissions,
        List<PermissionOperationResult> failedPermissions,
        boolean rollbackAttempted,
        boolean rollbackSucceeded,
        List<String> rolledBackPermissions,
        List<PermissionOperationResult> rollbackFailures,
        String reason
    ) {
        return new PermissionBatchResult(
            providerName,
            false,
            true,
            attemptedPermissions,
            successfulPermissions,
            failedPermissions,
            rollbackAttempted,
            rollbackSucceeded,
            rolledBackPermissions,
            rollbackFailures,
            reason
        );
    }
}
