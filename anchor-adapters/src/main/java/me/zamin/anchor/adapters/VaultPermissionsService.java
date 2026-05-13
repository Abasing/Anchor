package me.zamin.anchor.adapters;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import me.zamin.anchor.api.ServiceStatus;
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
    public Set<String> getGroups(UUID playerId) {
        String[] groups = permission.getPlayerGroups(null, Bukkit.getOfflinePlayer(playerId));
        return groups == null ? Set.of() : new LinkedHashSet<>(Arrays.asList(groups));
    }

    @Override
    public Optional<String> getPrimaryGroup(UUID playerId) {
        return Optional.ofNullable(permission.getPrimaryGroup(null, Bukkit.getOfflinePlayer(playerId)));
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
}
