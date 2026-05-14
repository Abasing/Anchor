package me.zamin.anchor.internal;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.zamin.anchor.api.permissions.PermissionBatchOptions;
import me.zamin.anchor.api.permissions.PermissionBatchResult;
import me.zamin.anchor.api.ServiceStatus;
import me.zamin.anchor.api.permissions.PermissionResult;
import me.zamin.anchor.api.permissions.PermissionsService;
import me.zamin.anchor.api.scheduler.SchedulerService;
import org.bukkit.entity.Player;

final class AsyncPermissionsServiceDecorator implements PermissionsService {

    private final PermissionsService delegate;
    private final SchedulerService scheduler;
    private final boolean nativeAsyncMutations;

    AsyncPermissionsServiceDecorator(PermissionsService delegate, SchedulerService scheduler, boolean nativeAsyncMutations) {
        this.delegate = delegate;
        this.scheduler = scheduler;
        this.nativeAsyncMutations = nativeAsyncMutations;
    }

    @Override
    public boolean has(UUID playerId, String permission) {
        return delegate.has(playerId, permission);
    }

    @Override
    public boolean has(UUID playerId, String world, String permission) {
        return delegate.has(playerId, world, permission);
    }

    @Override
    public boolean has(Player player, String permission) {
        return delegate.has(player, permission);
    }

    @Override
    public boolean has(Player player, String world, String permission) {
        return delegate.has(player, world, permission);
    }

    @Override
    public Set<String> getGroups(UUID playerId) {
        return delegate.getGroups(playerId);
    }

    @Override
    public Optional<String> getPrimaryGroup(UUID playerId) {
        return delegate.getPrimaryGroup(playerId);
    }

    @Override
    public PermissionResult grant(UUID playerId, String permission) {
        return delegate.grant(playerId, permission);
    }

    @Override
    public PermissionResult revoke(UUID playerId, String permission) {
        return delegate.revoke(playerId, permission);
    }

    @Override
    public PermissionResult grant(UUID playerId, String world, String permission) {
        return delegate.grant(playerId, world, permission);
    }

    @Override
    public PermissionResult revoke(UUID playerId, String world, String permission) {
        return delegate.revoke(playerId, world, permission);
    }

    @Override
    public CompletableFuture<PermissionResult> grantAsync(UUID playerId, String permission) {
        return delegateAsync(() -> delegate.grant(playerId, permission), delegate.grantAsync(playerId, permission));
    }

    @Override
    public CompletableFuture<PermissionResult> revokeAsync(UUID playerId, String permission) {
        return delegateAsync(() -> delegate.revoke(playerId, permission), delegate.revokeAsync(playerId, permission));
    }

    @Override
    public CompletableFuture<PermissionResult> grantAsync(UUID playerId, String world, String permission) {
        return delegateAsync(() -> delegate.grant(playerId, world, permission), delegate.grantAsync(playerId, world, permission));
    }

    @Override
    public CompletableFuture<PermissionResult> revokeAsync(UUID playerId, String world, String permission) {
        return delegateAsync(() -> delegate.revoke(playerId, world, permission), delegate.revokeAsync(playerId, world, permission));
    }

    @Override
    public CompletableFuture<PermissionBatchResult> grantAllAsync(UUID playerId, java.util.Collection<String> permissions) {
        return batchAsync(() -> delegate.grantAllAsync(playerId, permissions).join(), delegate.grantAllAsync(playerId, permissions));
    }

    @Override
    public CompletableFuture<PermissionBatchResult> grantAllAsync(UUID playerId, java.util.Collection<String> permissions, PermissionBatchOptions options) {
        return batchAsync(() -> delegate.grantAllAsync(playerId, permissions, options).join(), delegate.grantAllAsync(playerId, permissions, options));
    }

    @Override
    public CompletableFuture<PermissionBatchResult> grantAllAsync(UUID playerId, String world, java.util.Collection<String> permissions) {
        return batchAsync(() -> delegate.grantAllAsync(playerId, world, permissions).join(), delegate.grantAllAsync(playerId, world, permissions));
    }

    @Override
    public CompletableFuture<PermissionBatchResult> grantAllAsync(UUID playerId, String world, java.util.Collection<String> permissions, PermissionBatchOptions options) {
        return batchAsync(() -> delegate.grantAllAsync(playerId, world, permissions, options).join(), delegate.grantAllAsync(playerId, world, permissions, options));
    }

    @Override
    public CompletableFuture<PermissionBatchResult> revokeAllAsync(UUID playerId, java.util.Collection<String> permissions) {
        return batchAsync(() -> delegate.revokeAllAsync(playerId, permissions).join(), delegate.revokeAllAsync(playerId, permissions));
    }

    @Override
    public CompletableFuture<PermissionBatchResult> revokeAllAsync(UUID playerId, java.util.Collection<String> permissions, PermissionBatchOptions options) {
        return batchAsync(() -> delegate.revokeAllAsync(playerId, permissions, options).join(), delegate.revokeAllAsync(playerId, permissions, options));
    }

    @Override
    public CompletableFuture<PermissionBatchResult> revokeAllAsync(UUID playerId, String world, java.util.Collection<String> permissions) {
        return batchAsync(() -> delegate.revokeAllAsync(playerId, world, permissions).join(), delegate.revokeAllAsync(playerId, world, permissions));
    }

    @Override
    public CompletableFuture<PermissionBatchResult> revokeAllAsync(UUID playerId, String world, java.util.Collection<String> permissions, PermissionBatchOptions options) {
        return batchAsync(() -> delegate.revokeAllAsync(playerId, world, permissions, options).join(), delegate.revokeAllAsync(playerId, world, permissions, options));
    }

    @Override
    public boolean isAvailable() {
        return delegate.isAvailable();
    }

    @Override
    public String providerName() {
        return delegate.providerName();
    }

    @Override
    public ServiceStatus status() {
        return delegate.status();
    }

    private CompletableFuture<PermissionResult> delegateAsync(SupplierWithResult fallbackSupplier, CompletableFuture<PermissionResult> nativeFuture) {
        if (nativeAsyncMutations) {
            return nativeFuture;
        }
        return scheduler.async().supplyAsync(fallbackSupplier::get);
    }

    private CompletableFuture<PermissionBatchResult> batchAsync(BatchSupplier fallbackSupplier, CompletableFuture<PermissionBatchResult> nativeFuture) {
        if (nativeAsyncMutations) {
            return nativeFuture;
        }
        return scheduler.async().supplyAsync(fallbackSupplier::get);
    }

    @FunctionalInterface
    private interface SupplierWithResult {
        PermissionResult get();
    }

    @FunctionalInterface
    private interface BatchSupplier {
        PermissionBatchResult get();
    }
}
