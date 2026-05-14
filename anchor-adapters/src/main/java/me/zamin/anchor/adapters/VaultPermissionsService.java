package me.zamin.anchor.adapters;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import me.zamin.anchor.api.ServiceStatus;
import me.zamin.anchor.api.permissions.PermissionResult;
import me.zamin.anchor.api.permissions.PermissionsService;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class VaultPermissionsService implements PermissionsService {

    private final Permission permission;

    public VaultPermissionsService(Permission permission) {
        this.permission = permission;
    }

    @Override
    public boolean has(UUID playerId, String permissionNode) {
        return permission.playerHas((String) null, Bukkit.getOfflinePlayer(playerId), permissionNode);
    }

    @Override
    public boolean has(Player player, String permissionNode) {
        return player != null && permission.playerHas(player.getWorld().getName(), player, permissionNode);
    }

    @Override
    public boolean has(UUID playerId, String world, String permissionNode) {
        return permission.playerHas(world, Bukkit.getOfflinePlayer(playerId), permissionNode);
    }

    @Override
    public boolean has(Player player, String world, String permissionNode) {
        return player != null && permission.playerHas(world, player, permissionNode);
    }

    @Override
    public Set<String> getGroups(UUID playerId) {
        String[] groups = permission.getPlayerGroups(null, Bukkit.getOfflinePlayer(playerId));
        return groups == null ? Set.of() : new LinkedHashSet<>(Arrays.asList(groups));
    }

    @Override
    public Optional<String> getPrimaryGroup(UUID playerId) {
        return Optional.ofNullable(permission.getPrimaryGroup(null, Bukkit.getOfflinePlayer(playerId)));
    }

    @Override
    public PermissionResult grant(UUID playerId, String permissionNode) {
        return mutate(permission.playerAdd((String) null, Bukkit.getOfflinePlayer(playerId), permissionNode), false, null, permissionNode);
    }

    @Override
    public PermissionResult revoke(UUID playerId, String permissionNode) {
        return mutate(permission.playerRemove((String) null, Bukkit.getOfflinePlayer(playerId), permissionNode), true, null, permissionNode);
    }

    @Override
    public PermissionResult grant(UUID playerId, String world, String permissionNode) {
        return mutate(permission.playerAdd(world, Bukkit.getOfflinePlayer(playerId), permissionNode), false, world, permissionNode);
    }

    @Override
    public PermissionResult revoke(UUID playerId, String world, String permissionNode) {
        return mutate(permission.playerRemove(world, Bukkit.getOfflinePlayer(playerId), permissionNode), true, world, permissionNode);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String providerName() {
        return "Vault/" + permission.getName();
    }

    @Override
    public ServiceStatus status() {
        return ServiceStatus.AVAILABLE;
    }

    private PermissionResult mutate(boolean success, boolean revoke, String world, String permissionNode) {
        if (success) {
            String scope = world == null ? "global" : "world=" + world;
            return PermissionResult.success(providerName(), (revoke ? "Revoked " : "Granted ") + permissionNode + " in " + scope + ".");
        }
        String scope = world == null ? "global" : "world=" + world;
        return PermissionResult.failure(providerName(), "Vault provider returned false while attempting to " + (revoke ? "revoke " : "grant ") + permissionNode + " in " + scope + ".");
    }
}
