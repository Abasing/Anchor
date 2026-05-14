package me.zamin.anchor.adapters;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.zamin.anchor.api.ServiceStatus;
import me.zamin.anchor.api.permissions.PermissionResult;
import me.zamin.anchor.api.permissions.PermissionsService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class BukkitPermissionsService implements PermissionsService {

    @Override
    public boolean has(UUID playerId, String permission) {
        Player player = Bukkit.getPlayer(playerId);
        return player != null && player.hasPermission(permission);
    }

    @Override
    public boolean has(Player player, String permission) {
        return player != null && player.hasPermission(permission);
    }

    @Override
    public boolean has(UUID playerId, String world, String permission) {
        Player player = Bukkit.getPlayer(playerId);
        return player != null && player.getWorld().getName().equalsIgnoreCase(world) && player.hasPermission(permission);
    }

    @Override
    public boolean has(Player player, String world, String permission) {
        return player != null && player.getWorld().getName().equalsIgnoreCase(world) && player.hasPermission(permission);
    }

    @Override
    public Set<String> getGroups(UUID playerId) {
        return Collections.emptySet();
    }

    @Override
    public Optional<String> getPrimaryGroup(UUID playerId) {
        return Optional.empty();
    }

    @Override
    public PermissionResult grant(UUID playerId, String permission) {
        return PermissionResult.unsupported(providerName(), "Bukkit fallback permissions do not support mutation.");
    }

    @Override
    public PermissionResult revoke(UUID playerId, String permission) {
        return PermissionResult.unsupported(providerName(), "Bukkit fallback permissions do not support mutation.");
    }

    @Override
    public PermissionResult grant(UUID playerId, String world, String permission) {
        return PermissionResult.unsupported(providerName(), "Bukkit fallback permissions do not support world-aware mutation.");
    }

    @Override
    public PermissionResult revoke(UUID playerId, String world, String permission) {
        return PermissionResult.unsupported(providerName(), "Bukkit fallback permissions do not support world-aware mutation.");
    }

    @Override
    public CompletableFuture<PermissionResult> grantAsync(UUID playerId, String permission) {
        return CompletableFuture.completedFuture(grant(playerId, permission));
    }

    @Override
    public CompletableFuture<PermissionResult> revokeAsync(UUID playerId, String permission) {
        return CompletableFuture.completedFuture(revoke(playerId, permission));
    }

    @Override
    public CompletableFuture<PermissionResult> grantAsync(UUID playerId, String world, String permission) {
        return CompletableFuture.completedFuture(grant(playerId, world, permission));
    }

    @Override
    public CompletableFuture<PermissionResult> revokeAsync(UUID playerId, String world, String permission) {
        return CompletableFuture.completedFuture(revoke(playerId, world, permission));
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String providerName() {
        return "Bukkit";
    }

    @Override
    public ServiceStatus status() {
        return ServiceStatus.FALLBACK;
    }
}
