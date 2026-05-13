package dev.anchor.permissions;

import dev.anchor.core.ProviderPriority;
import dev.anchor.core.ServiceStatus;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class BukkitPermissionsProvider implements PermissionsProvider {

    @Override
    public boolean has(UUID playerId, String permission) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        return player.isOnline() && has(player.getPlayer(), permission);
    }

    @Override
    public boolean has(Player player, String permission) {
        return player != null && permission != null && player.hasPermission(permission);
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
    public String getProviderName() {
        return "Bukkit";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public ServiceStatus getStatus() {
        return ServiceStatus.FALLBACK;
    }

    @Override
    public ProviderPriority getPriority() {
        return ProviderPriority.LOW;
    }
}
