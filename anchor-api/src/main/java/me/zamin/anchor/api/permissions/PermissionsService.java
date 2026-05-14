package me.zamin.anchor.api.permissions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import me.zamin.anchor.api.AnchorService;
import org.bukkit.entity.Player;

/**
 * Stable abstraction over LuckPerms, Vault permissions, and Bukkit fallback
 * permission checks.
 */
public interface PermissionsService extends AnchorService {

    /**
     * Checks a permission by UUID.
     *
     * @param playerId non-null player UUID
     * @param permission non-null permission node
     * @return {@code true} when granted
     */
    boolean has(UUID playerId, String permission);

    /**
     * Checks a permission by UUID in a world-specific context when the backing
     * provider supports world-aware resolution.
     * <p>
     * On providers without world-aware contexts this falls back to the global
     * permission check. Safe on Paper and Folia. No async work is performed by
     * this contract, though providers may rely on cached permission state.
     *
     * @param playerId non-null player UUID
     * @param world non-null world name
     * @param permission non-null permission node
     * @return {@code true} when granted in the requested world context
     */
    default boolean has(UUID playerId, String world, String permission) {
        return has(playerId, permission);
    }

    /**
     * Checks a permission directly on a live player instance.
     *
     * @param player non-null player instance
     * @param permission non-null permission node
     * @return {@code true} when granted
     */
    boolean has(Player player, String permission);

    /**
     * Checks a permission directly on a live player instance in a world-specific
     * context when the backing provider supports world-aware resolution.
     * <p>
     * On providers without world-aware contexts this falls back to the global
     * player permission check. Safe on Paper and Folia. No async work is
     * performed by this contract.
     *
     * @param player non-null player instance
     * @param world non-null world name
     * @param permission non-null permission node
     * @return {@code true} when granted in the requested world context
     */
    default boolean has(Player player, String world, String permission) {
        return has(player, permission);
    }

    /**
     * Returns the known groups for a player.
     *
     * @param playerId non-null player UUID
     * @return non-null group set, possibly empty on fallback providers
     */
    Set<String> getGroups(UUID playerId);

    /**
     * Returns the primary group if the backing provider supports it.
     *
     * @param playerId non-null player UUID
     * @return optional primary group
     */
    Optional<String> getPrimaryGroup(UUID playerId);

    /**
     * Grants a global permission to a player when the backing provider supports
     * permission mutation.
     * <p>
     * Providers that only support permission checks return an unsupported
     * result instead of throwing. This method may block on provider storage or
     * network work and should not be used for bulk mutations or hot-path
     * gameplay logic. Prefer {@link #grantAsync(UUID, String)} for command
     * handlers, migrations, or any operation that may touch provider-managed
     * persistence.
     *
     * @param playerId non-null player UUID
     * @param permission non-null permission node
     * @return non-null mutation result
     */
    default PermissionResult grant(UUID playerId, String permission) {
        return PermissionResult.unsupported(providerName(), "Permission mutation is not supported by this provider.");
    }

    /**
     * Revokes a global permission from a player when the backing provider
     * supports permission mutation.
     * <p>
     * This method may block on provider storage or network work. Prefer
     * {@link #revokeAsync(UUID, String)} for bulk or command-driven changes.
     *
     * @param playerId non-null player UUID
     * @param permission non-null permission node
     * @return non-null mutation result
     */
    default PermissionResult revoke(UUID playerId, String permission) {
        return PermissionResult.unsupported(providerName(), "Permission mutation is not supported by this provider.");
    }

    /**
     * Grants a world-specific permission to a player when the backing provider
     * supports world-aware mutation.
     * <p>
     * This method may block on provider storage or network work. Prefer
     * {@link #grantAsync(UUID, String, String)} when the provider may need to
     * touch persistence or load offline user state.
     *
     * @param playerId non-null player UUID
     * @param world non-null world name
     * @param permission non-null permission node
     * @return non-null mutation result
     */
    default PermissionResult grant(UUID playerId, String world, String permission) {
        return PermissionResult.unsupported(providerName(), "World-aware permission mutation is not supported by this provider.");
    }

    /**
     * Revokes a world-specific permission from a player when the backing
     * provider supports world-aware mutation.
     * <p>
     * This method may block on provider storage or network work. Prefer
     * {@link #revokeAsync(UUID, String, String)} for non-trivial workflows.
     *
     * @param playerId non-null player UUID
     * @param world non-null world name
     * @param permission non-null permission node
     * @return non-null mutation result
     */
    default PermissionResult revoke(UUID playerId, String world, String permission) {
        return PermissionResult.unsupported(providerName(), "World-aware permission mutation is not supported by this provider.");
    }

    /**
     * Grants a global permission asynchronously.
     * <p>
     * This is the preferred mutation path for command handlers, migrations, and
     * bulk operations. Providers with native async APIs may use them directly.
     * Providers without native async support may still complete this future by
     * moving sync work off the main server thread. The returned future always
     * completes with a {@link PermissionResult}; unsupported providers do not
     * throw and instead return an unsupported result.
     *
     * @param playerId non-null player UUID
     * @param permission non-null permission node
     * @return non-null future that completes with the mutation result
     */
    default CompletableFuture<PermissionResult> grantAsync(UUID playerId, String permission) {
        return CompletableFuture.completedFuture(grant(playerId, permission));
    }

    /**
     * Revokes a global permission asynchronously.
     * <p>
     * This is the preferred mutation path for command handlers, migrations, and
     * bulk operations. Provider behavior may vary depending on whether the
     * backing system supports true async mutation or an off-thread sync bridge.
     *
     * @param playerId non-null player UUID
     * @param permission non-null permission node
     * @return non-null future that completes with the mutation result
     */
    default CompletableFuture<PermissionResult> revokeAsync(UUID playerId, String permission) {
        return CompletableFuture.completedFuture(revoke(playerId, permission));
    }

    /**
     * Grants a world-specific permission asynchronously.
     * <p>
     * This is the preferred mutation path for command handlers, migrations, and
     * bulk operations that may require provider persistence work.
     *
     * @param playerId non-null player UUID
     * @param world non-null world name
     * @param permission non-null permission node
     * @return non-null future that completes with the mutation result
     */
    default CompletableFuture<PermissionResult> grantAsync(UUID playerId, String world, String permission) {
        return CompletableFuture.completedFuture(grant(playerId, world, permission));
    }

    /**
     * Revokes a world-specific permission asynchronously.
     * <p>
     * This is the preferred mutation path for command handlers, migrations, and
     * bulk operations that may require provider persistence work.
     *
     * @param playerId non-null player UUID
     * @param world non-null world name
     * @param permission non-null permission node
     * @return non-null future that completes with the mutation result
     */
    default CompletableFuture<PermissionResult> revokeAsync(UUID playerId, String world, String permission) {
        return CompletableFuture.completedFuture(revoke(playerId, world, permission));
    }

    /**
     * Grants multiple global permissions asynchronously using the default batch
     * behavior.
     * <p>
     * Batch operations are not database transactions. Check the returned result
     * for partial success and rollback state.
     *
     * @param playerId non-null player UUID
     * @param permissions non-null collection of permission nodes
     * @return non-null future containing the batch result
     */
    default CompletableFuture<PermissionBatchResult> grantAllAsync(UUID playerId, Collection<String> permissions) {
        return grantAllAsync(playerId, permissions, PermissionBatchOptions.defaults());
    }

    /**
     * Grants multiple global permissions asynchronously with explicit batch
     * options.
     * <p>
     * Rollback is best-effort only. Providers may vary in how much state can be
     * reverted safely after a partial failure.
     *
     * @param playerId non-null player UUID
     * @param permissions non-null collection of permission nodes
     * @param options non-null batch options
     * @return non-null future containing the batch result
     */
    default CompletableFuture<PermissionBatchResult> grantAllAsync(UUID playerId, Collection<String> permissions, PermissionBatchOptions options) {
        List<String> attemptedPermissions = normalizeBatchPermissions(permissions);
        return mutateBatchAsync(
            attemptedPermissions,
            Objects.requireNonNull(options, "options"),
            permission -> grantAsync(playerId, permission),
            permission -> revokeAsync(playerId, permission),
            false
        );
    }

    /**
     * Grants multiple world-aware permissions asynchronously using the default
     * batch behavior.
     *
     * @param playerId non-null player UUID
     * @param world non-null world name
     * @param permissions non-null collection of permission nodes
     * @return non-null future containing the batch result
     */
    default CompletableFuture<PermissionBatchResult> grantAllAsync(UUID playerId, String world, Collection<String> permissions) {
        return grantAllAsync(playerId, world, permissions, PermissionBatchOptions.defaults());
    }

    /**
     * Grants multiple world-aware permissions asynchronously with explicit
     * batch options.
     *
     * @param playerId non-null player UUID
     * @param world non-null world name
     * @param permissions non-null collection of permission nodes
     * @param options non-null batch options
     * @return non-null future containing the batch result
     */
    default CompletableFuture<PermissionBatchResult> grantAllAsync(UUID playerId, String world, Collection<String> permissions, PermissionBatchOptions options) {
        List<String> attemptedPermissions = normalizeBatchPermissions(permissions);
        return mutateBatchAsync(
            attemptedPermissions,
            Objects.requireNonNull(options, "options"),
            permission -> grantAsync(playerId, world, permission),
            permission -> revokeAsync(playerId, world, permission),
            false
        );
    }

    /**
     * Revokes multiple global permissions asynchronously using the default
     * batch behavior.
     *
     * @param playerId non-null player UUID
     * @param permissions non-null collection of permission nodes
     * @return non-null future containing the batch result
     */
    default CompletableFuture<PermissionBatchResult> revokeAllAsync(UUID playerId, Collection<String> permissions) {
        return revokeAllAsync(playerId, permissions, PermissionBatchOptions.defaults());
    }

    /**
     * Revokes multiple global permissions asynchronously with explicit batch
     * options.
     *
     * @param playerId non-null player UUID
     * @param permissions non-null collection of permission nodes
     * @param options non-null batch options
     * @return non-null future containing the batch result
     */
    default CompletableFuture<PermissionBatchResult> revokeAllAsync(UUID playerId, Collection<String> permissions, PermissionBatchOptions options) {
        List<String> attemptedPermissions = normalizeBatchPermissions(permissions);
        return mutateBatchAsync(
            attemptedPermissions,
            Objects.requireNonNull(options, "options"),
            permission -> revokeAsync(playerId, permission),
            permission -> grantAsync(playerId, permission),
            true
        );
    }

    /**
     * Revokes multiple world-aware permissions asynchronously using the default
     * batch behavior.
     *
     * @param playerId non-null player UUID
     * @param world non-null world name
     * @param permissions non-null collection of permission nodes
     * @return non-null future containing the batch result
     */
    default CompletableFuture<PermissionBatchResult> revokeAllAsync(UUID playerId, String world, Collection<String> permissions) {
        return revokeAllAsync(playerId, world, permissions, PermissionBatchOptions.defaults());
    }

    /**
     * Revokes multiple world-aware permissions asynchronously with explicit
     * batch options.
     *
     * @param playerId non-null player UUID
     * @param world non-null world name
     * @param permissions non-null collection of permission nodes
     * @param options non-null batch options
     * @return non-null future containing the batch result
     */
    default CompletableFuture<PermissionBatchResult> revokeAllAsync(UUID playerId, String world, Collection<String> permissions, PermissionBatchOptions options) {
        List<String> attemptedPermissions = normalizeBatchPermissions(permissions);
        return mutateBatchAsync(
            attemptedPermissions,
            Objects.requireNonNull(options, "options"),
            permission -> revokeAsync(playerId, world, permission),
            permission -> grantAsync(playerId, world, permission),
            true
        );
    }

    private CompletableFuture<PermissionBatchResult> mutateBatchAsync(
        List<String> attemptedPermissions,
        PermissionBatchOptions options,
        Function<String, CompletableFuture<PermissionResult>> mutate,
        Function<String, CompletableFuture<PermissionResult>> rollbackMutate,
        boolean revokeOperation
    ) {
        if (attemptedPermissions.isEmpty()) {
            return CompletableFuture.completedFuture(PermissionBatchResult.success(
                providerName(),
                attemptedPermissions,
                List.of(),
                revokeOperation ? "No permissions were provided for batch revoke." : "No permissions were provided for batch grant."
            ));
        }

        List<String> successfulPermissions = new ArrayList<>();
        List<PermissionOperationResult> failedPermissions = new ArrayList<>();
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);

        for (String permission : attemptedPermissions) {
            chain = chain.thenCompose(ignored -> {
                if (!failedPermissions.isEmpty() && (options.rollbackOnFailure() || !options.continueOnFailure())) {
                    return CompletableFuture.completedFuture(null);
                }
                return mutate.apply(permission).thenAccept(result -> {
                    if (result.success()) {
                        successfulPermissions.add(permission);
                    } else {
                        failedPermissions.add(PermissionOperationResult.from(permission, result));
                    }
                });
            });
        }

        return chain.thenCompose(ignored -> {
            if (failedPermissions.isEmpty()) {
                return CompletableFuture.completedFuture(PermissionBatchResult.success(
                    providerName(),
                    attemptedPermissions,
                    successfulPermissions,
                    batchSuccessReason(revokeOperation, successfulPermissions.size())
                ));
            }

            if (!options.rollbackOnFailure() || successfulPermissions.isEmpty()) {
                return CompletableFuture.completedFuture(PermissionBatchResult.failure(
                    providerName(),
                    attemptedPermissions,
                    successfulPermissions,
                    failedPermissions,
                    false,
                    false,
                    List.of(),
                    List.of(),
                    batchFailureReason(revokeOperation, failedPermissions.size(), successfulPermissions.size(), options)
                ));
            }

            List<String> rollbackTargets = new ArrayList<>(successfulPermissions);
            Collections.reverse(rollbackTargets);
            return rollbackBatchAsync(rollbackTargets, rollbackMutate).thenApply(rollback -> PermissionBatchResult.failure(
                providerName(),
                attemptedPermissions,
                successfulPermissions,
                failedPermissions,
                true,
                rollback.rollbackFailures().isEmpty(),
                rollback.rolledBackPermissions(),
                rollback.rollbackFailures(),
                rollbackFailureReason(revokeOperation, failedPermissions.size(), rollback)
            ));
        });
    }

    private static CompletableFuture<RollbackState> rollbackBatchAsync(
        List<String> rollbackTargets,
        Function<String, CompletableFuture<PermissionResult>> rollbackMutate
    ) {
        List<String> rolledBackPermissions = new ArrayList<>();
        List<PermissionOperationResult> rollbackFailures = new ArrayList<>();
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);

        for (String permission : rollbackTargets) {
            chain = chain.thenCompose(ignored -> rollbackMutate.apply(permission).thenAccept(result -> {
                if (result.success()) {
                    rolledBackPermissions.add(permission);
                } else {
                    rollbackFailures.add(PermissionOperationResult.from(permission, result));
                }
            }));
        }

        return chain.thenApply(ignored -> new RollbackState(List.copyOf(rolledBackPermissions), List.copyOf(rollbackFailures)));
    }

    private static List<String> normalizeBatchPermissions(Collection<String> permissions) {
        Objects.requireNonNull(permissions, "permissions");
        List<String> normalized = new ArrayList<>(permissions.size());
        for (String permission : permissions) {
            normalized.add(Objects.requireNonNull(permission, "permission"));
        }
        return List.copyOf(normalized);
    }

    private static String batchSuccessReason(boolean revokeOperation, int successfulCount) {
        return (revokeOperation ? "Revoked " : "Granted ") + successfulCount + " permission(s) successfully.";
    }

    private static String batchFailureReason(boolean revokeOperation, int failedCount, int successfulCount, PermissionBatchOptions options) {
        String action = revokeOperation ? "revoke" : "grant";
        if (options.continueOnFailure()) {
            return "Batch " + action + " completed with " + successfulCount + " success(es) and " + failedCount + " failure(s).";
        }
        return "Batch " + action + " stopped after " + failedCount + " failure(s) and " + successfulCount + " success(es).";
    }

    private static String rollbackFailureReason(boolean revokeOperation, int failedCount, RollbackState rollbackState) {
        String action = revokeOperation ? "revoke" : "grant";
        if (rollbackState.rollbackFailures().isEmpty()) {
            return "Batch " + action + " failed after " + failedCount + " failure(s). Best-effort rollback succeeded for previously successful permissions.";
        }
        return "Batch " + action + " failed after " + failedCount + " failure(s). Rollback was attempted but did not fully succeed.";
    }
}
