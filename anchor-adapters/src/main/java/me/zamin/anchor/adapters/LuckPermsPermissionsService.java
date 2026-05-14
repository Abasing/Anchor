package me.zamin.anchor.adapters;

import java.util.Collections;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.zamin.anchor.api.ServiceStatus;
import me.zamin.anchor.api.permissions.PermissionBatchOptions;
import me.zamin.anchor.api.permissions.PermissionBatchResult;
import me.zamin.anchor.api.permissions.PermissionOperationResult;
import me.zamin.anchor.api.permissions.PermissionResult;
import me.zamin.anchor.api.permissions.PermissionsService;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class LuckPermsPermissionsService implements PermissionsService {

    private final LuckPerms luckPerms;

    public LuckPermsPermissionsService(LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
    }

    @Override
    public boolean has(UUID playerId, String permission) {
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) {
            Player player = Bukkit.getPlayer(playerId);
            return player != null && player.hasPermission(permission);
        }
        QueryOptions queryOptions = luckPerms.getContextManager().getQueryOptions(user)
            .orElse(luckPerms.getContextManager().getStaticQueryOptions());
        CachedPermissionData data = user.getCachedData().getPermissionData(queryOptions);
        return data.checkPermission(permission).asBoolean();
    }

    @Override
    public boolean has(UUID playerId, String world, String permission) {
        if (world == null || world.isBlank()) {
            return has(playerId, permission);
        }
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) {
            Player player = Bukkit.getPlayer(playerId);
            return player != null
                && player.getWorld().getName().equalsIgnoreCase(world)
                && player.hasPermission(permission);
        }
        CachedPermissionData data = user.getCachedData().getPermissionData(worldQueryOptions(world));
        return data.checkPermission(permission).asBoolean();
    }

    @Override
    public boolean has(Player player, String permission) {
        return player != null && has(player.getUniqueId(), permission);
    }

    @Override
    public boolean has(Player player, String world, String permission) {
        return player != null && has(player.getUniqueId(), world, permission);
    }

    @Override
    public Set<String> getGroups(UUID playerId) {
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) {
            return Collections.emptySet();
        }
        Set<String> groups = new LinkedHashSet<>();
        for (InheritanceNode node : user.getNodes(NodeType.INHERITANCE)) {
            groups.add(node.getGroupName());
        }
        return Collections.unmodifiableSet(groups);
    }

    @Override
    public Optional<String> getPrimaryGroup(UUID playerId) {
        User user = luckPerms.getUserManager().getUser(playerId);
        return user == null ? Optional.empty() : Optional.ofNullable(user.getPrimaryGroup());
    }

    @Override
    public PermissionResult grant(UUID playerId, String permission) {
        return grantAsync(playerId, permission).join();
    }

    @Override
    public PermissionResult revoke(UUID playerId, String permission) {
        return revokeAsync(playerId, permission).join();
    }

    @Override
    public PermissionResult grant(UUID playerId, String world, String permission) {
        return grantAsync(playerId, world, permission).join();
    }

    @Override
    public PermissionResult revoke(UUID playerId, String world, String permission) {
        return revokeAsync(playerId, world, permission).join();
    }

    @Override
    public CompletableFuture<PermissionResult> grantAsync(UUID playerId, String permission) {
        return mutateAsync(playerId, buildNode(permission, null), false, null);
    }

    @Override
    public CompletableFuture<PermissionResult> revokeAsync(UUID playerId, String permission) {
        return mutateAsync(playerId, buildNode(permission, null), true, null);
    }

    @Override
    public CompletableFuture<PermissionResult> grantAsync(UUID playerId, String world, String permission) {
        return mutateAsync(playerId, buildNode(permission, world), false, world);
    }

    @Override
    public CompletableFuture<PermissionResult> revokeAsync(UUID playerId, String world, String permission) {
        return mutateAsync(playerId, buildNode(permission, world), true, world);
    }

    @Override
    public CompletableFuture<PermissionBatchResult> grantAllAsync(UUID playerId, Collection<String> permissions, PermissionBatchOptions options) {
        return mutateBatchAsync(playerId, null, permissions, options, false);
    }

    @Override
    public CompletableFuture<PermissionBatchResult> grantAllAsync(UUID playerId, String world, Collection<String> permissions, PermissionBatchOptions options) {
        return mutateBatchAsync(playerId, world, permissions, options, false);
    }

    @Override
    public CompletableFuture<PermissionBatchResult> revokeAllAsync(UUID playerId, Collection<String> permissions, PermissionBatchOptions options) {
        return mutateBatchAsync(playerId, null, permissions, options, true);
    }

    @Override
    public CompletableFuture<PermissionBatchResult> revokeAllAsync(UUID playerId, String world, Collection<String> permissions, PermissionBatchOptions options) {
        return mutateBatchAsync(playerId, world, permissions, options, true);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String providerName() {
        return "LuckPerms";
    }

    @Override
    public ServiceStatus status() {
        return ServiceStatus.AVAILABLE;
    }

    private QueryOptions worldQueryOptions(String world) {
        ImmutableContextSet context = luckPerms.getContextManager().getContextSetFactory().immutableOf("world", world);
        return QueryOptions.contextual(context);
    }

    private Node buildNode(String permission, String world) {
        if (world == null || world.isBlank()) {
            return Node.builder(permission).build();
        }
        return Node.builder(permission).withContext("world", world).build();
    }

    private CompletableFuture<PermissionResult> mutateAsync(UUID playerId, Node node, boolean revoke, String world) {
        boolean loadedBefore = luckPerms.getUserManager().isLoaded(playerId);
        return luckPerms.getUserManager().loadUser(playerId)
            .thenCompose(user -> {
                DataMutateResult result = revoke ? user.data().remove(node) : user.data().add(node);
                if (result.wasSuccessful()) {
                    return luckPerms.getUserManager().saveUser(user)
                        .thenApply(ignored -> PermissionResult.success(providerName(), mutationMessage(revoke, node.getKey(), world)));
                }
                return CompletableFuture.completedFuture(PermissionResult.failure(providerName(), mutateFailureReason(revoke, result, node.getKey(), world)));
            })
            .exceptionally(exception -> PermissionResult.failure(providerName(), "LuckPerms mutation failed: " + rootMessage(exception)))
            .whenComplete((ignored, throwable) -> {
                if (!loadedBefore) {
                    User user = luckPerms.getUserManager().getUser(playerId);
                    if (user != null) {
                        luckPerms.getUserManager().cleanupUser(user);
                    }
                }
            });
    }

    private CompletableFuture<PermissionBatchResult> mutateBatchAsync(
        UUID playerId,
        String world,
        Collection<String> permissions,
        PermissionBatchOptions options,
        boolean revoke
    ) {
        List<String> attemptedPermissions = List.copyOf(permissions);
        if (attemptedPermissions.isEmpty()) {
            return CompletableFuture.completedFuture(PermissionBatchResult.success(
                providerName(),
                attemptedPermissions,
                List.of(),
                revoke ? "No permissions were provided for batch revoke." : "No permissions were provided for batch grant."
            ));
        }

        boolean loadedBefore = luckPerms.getUserManager().isLoaded(playerId);
        return luckPerms.getUserManager().loadUser(playerId)
            .thenCompose(user -> applyBatchMutations(user, attemptedPermissions, world, options, revoke))
            .exceptionally(exception -> PermissionBatchResult.failure(
                providerName(),
                attemptedPermissions,
                List.of(),
                List.of(new PermissionOperationResult("<batch>", false, true, "LuckPerms batch mutation failed: " + rootMessage(exception))),
                false,
                false,
                List.of(),
                List.of(),
                "LuckPerms batch mutation failed before completion."
            ))
            .whenComplete((ignored, throwable) -> {
                if (!loadedBefore) {
                    User user = luckPerms.getUserManager().getUser(playerId);
                    if (user != null) {
                        luckPerms.getUserManager().cleanupUser(user);
                    }
                }
            });
    }

    private CompletableFuture<PermissionBatchResult> applyBatchMutations(
        User user,
        List<String> attemptedPermissions,
        String world,
        PermissionBatchOptions options,
        boolean revoke
    ) {
        List<String> successfulPermissions = new java.util.ArrayList<>();
        List<PermissionOperationResult> failedPermissions = new java.util.ArrayList<>();
        Map<String, Node> successfulNodes = new HashMap<>();

        for (String permission : attemptedPermissions) {
            Node node = buildNode(permission, world);
            DataMutateResult result = revoke ? user.data().remove(node) : user.data().add(node);
            if (result.wasSuccessful()) {
                successfulPermissions.add(permission);
                successfulNodes.put(permission, node);
                continue;
            }

            failedPermissions.add(new PermissionOperationResult(permission, false, true, mutateFailureReason(revoke, result, permission, world)));
            if (options.rollbackOnFailure() || !options.continueOnFailure()) {
                break;
            }
        }

        if (failedPermissions.isEmpty()) {
            if (successfulPermissions.isEmpty()) {
                return CompletableFuture.completedFuture(PermissionBatchResult.success(
                    providerName(),
                    attemptedPermissions,
                    List.of(),
                    revoke ? "No permissions were changed by the batch revoke." : "No permissions were changed by the batch grant."
                ));
            }
            return luckPerms.getUserManager().saveUser(user)
                .thenApply(ignored -> PermissionBatchResult.success(
                    providerName(),
                    attemptedPermissions,
                    successfulPermissions,
                    (revoke ? "Revoked " : "Granted ") + successfulPermissions.size() + " permission(s) successfully."
                ))
                .exceptionally(exception -> PermissionBatchResult.failure(
                    providerName(),
                    attemptedPermissions,
                    successfulPermissions,
                    List.of(new PermissionOperationResult("<save>", false, true, "LuckPerms failed to save the batch mutation: " + rootMessage(exception))),
                    false,
                    false,
                    List.of(),
                    List.of(),
                    "LuckPerms failed while saving the batch mutation. Persisted state may be incomplete."
                ));
        }

        if (!options.rollbackOnFailure() || successfulPermissions.isEmpty()) {
            if (successfulPermissions.isEmpty()) {
                return CompletableFuture.completedFuture(PermissionBatchResult.failure(
                    providerName(),
                    attemptedPermissions,
                    List.of(),
                    failedPermissions,
                    false,
                    false,
                    List.of(),
                    List.of(),
                    "LuckPerms batch " + (revoke ? "revoke" : "grant") + " failed before any permission succeeded."
                ));
            }
            return luckPerms.getUserManager().saveUser(user)
                .thenApply(ignored -> PermissionBatchResult.failure(
                    providerName(),
                    attemptedPermissions,
                    successfulPermissions,
                    failedPermissions,
                    false,
                    false,
                    List.of(),
                    List.of(),
                    "LuckPerms batch " + (revoke ? "revoke" : "grant") + " completed with partial success."
                ))
                .exceptionally(exception -> PermissionBatchResult.failure(
                    providerName(),
                    attemptedPermissions,
                    successfulPermissions,
                    mergeFailureEntries(failedPermissions, new PermissionOperationResult("<save>", false, true, "LuckPerms failed to save the partial batch mutation: " + rootMessage(exception))),
                    false,
                    false,
                    List.of(),
                    List.of(),
                    "LuckPerms failed while saving a partially successful batch mutation."
                ));
        }

        List<String> rolledBackPermissions = new java.util.ArrayList<>();
        List<PermissionOperationResult> rollbackFailures = new java.util.ArrayList<>();
        List<String> rollbackTargets = new java.util.ArrayList<>(successfulPermissions);
        java.util.Collections.reverse(rollbackTargets);
        for (String permission : rollbackTargets) {
            Node node = successfulNodes.get(permission);
            DataMutateResult rollbackResult = revoke ? user.data().add(node) : user.data().remove(node);
            if (rollbackResult.wasSuccessful()) {
                rolledBackPermissions.add(permission);
            } else {
                rollbackFailures.add(new PermissionOperationResult(permission, false, true, rollbackFailureReason(revoke, rollbackResult, permission, world)));
            }
        }

        if (rollbackFailures.isEmpty()) {
            return CompletableFuture.completedFuture(PermissionBatchResult.failure(
                providerName(),
                attemptedPermissions,
                successfulPermissions,
                failedPermissions,
                true,
                true,
                rolledBackPermissions,
                List.of(),
                "LuckPerms batch " + (revoke ? "revoke" : "grant") + " failed and best-effort rollback succeeded."
            ));
        }

        return luckPerms.getUserManager().saveUser(user)
            .thenApply(ignored -> PermissionBatchResult.failure(
                providerName(),
                attemptedPermissions,
                successfulPermissions,
                failedPermissions,
                true,
                false,
                rolledBackPermissions,
                rollbackFailures,
                "LuckPerms batch " + (revoke ? "revoke" : "grant") + " failed and rollback did not fully succeed."
            ))
            .exceptionally(exception -> PermissionBatchResult.failure(
                providerName(),
                attemptedPermissions,
                successfulPermissions,
                failedPermissions,
                true,
                false,
                rolledBackPermissions,
                mergeFailureEntries(rollbackFailures, new PermissionOperationResult("<rollback-save>", false, true, "LuckPerms failed to save the rollback state: " + rootMessage(exception))),
                "LuckPerms batch " + (revoke ? "revoke" : "grant") + " failed and rollback persistence also failed."
            ));
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    private String mutationMessage(boolean revoke, String permission, String world) {
        String scope = world == null || world.isBlank() ? "globally" : "in world " + world;
        return (revoke ? "Revoked " : "Granted ") + permission + " " + scope + ".";
    }

    private String mutateFailureReason(boolean revoke, DataMutateResult result, String permission, String world) {
        String scope = world == null || world.isBlank() ? "global scope" : "world " + world;
        return switch (result) {
            case FAIL_ALREADY_HAS -> "LuckPerms already has " + permission + " in " + scope + ".";
            case FAIL_LACKS -> "LuckPerms could not remove " + permission + " because it is not present in " + scope + ".";
            case FAIL -> "LuckPerms returned a generic failure while attempting to " + (revoke ? "revoke " : "grant ") + permission + " in " + scope + ".";
            case SUCCESS -> mutationMessage(revoke, permission, world);
        };
    }

    private String rollbackFailureReason(boolean revoke, DataMutateResult result, String permission, String world) {
        String scope = world == null || world.isBlank() ? "global scope" : "world " + world;
        return switch (result) {
            case FAIL_ALREADY_HAS -> "LuckPerms rollback could not restore " + permission + " because it already exists in " + scope + ".";
            case FAIL_LACKS -> "LuckPerms rollback could not remove " + permission + " because it is not present in " + scope + ".";
            case FAIL -> "LuckPerms rollback returned a generic failure while attempting to " + (revoke ? "restore " : "remove ") + permission + " in " + scope + ".";
            case SUCCESS -> "LuckPerms rollback succeeded for " + permission + " in " + scope + ".";
        };
    }

    private List<PermissionOperationResult> mergeFailureEntries(List<PermissionOperationResult> failures, PermissionOperationResult extra) {
        List<PermissionOperationResult> merged = new java.util.ArrayList<>(failures);
        merged.add(extra);
        return List.copyOf(merged);
    }
}
