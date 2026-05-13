package dev.anchor.permissions;

import dev.anchor.core.ProviderPriority;
import dev.anchor.core.ServiceStatus;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class VaultPermissionsProvider implements PermissionsProvider {

    private final Permission permission;

    public VaultPermissionsProvider(Permission permission) {
        this.permission = permission;
    }

    @Override
    public boolean has(UUID playerId, String permissionNode) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        return permission.playerHas((String) null, offlinePlayer, permissionNode);
    }

    @Override
    public boolean has(Player player, String permissionNode) {
        return player != null && permission.playerHas(player.getWorld().getName(), player, permissionNode);
    }

    @Override
    public Set<String> getGroups(UUID playerId) {
        String[] groups = permission.getPlayerGroups(null, Bukkit.getOfflinePlayer(playerId));
        if (groups == null || groups.length == 0) {
            return Collections.emptySet();
        }
        return new LinkedHashSet<>(java.util.Arrays.asList(groups));
    }

    @Override
    public Optional<String> getPrimaryGroup(UUID playerId) {
        return Optional.ofNullable(permission.getPrimaryGroup(null, Bukkit.getOfflinePlayer(playerId)));
    }

    @Override
    public String getProviderName() {
        return "Vault/" + permission.getName();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public ServiceStatus getStatus() {
        return ServiceStatus.AVAILABLE;
    }

    @Override
    public ProviderPriority getPriority() {
        return ProviderPriority.NORMAL;
    }
}
