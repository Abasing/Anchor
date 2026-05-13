package me.zamin.anchor.adapters;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import me.zamin.anchor.api.ServiceStatus;
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
    public Set<String> getGroups(UUID playerId) {
        return Collections.emptySet();
    }

    @Override
    public Optional<String> getPrimaryGroup(UUID playerId) {
        return Optional.empty();
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
