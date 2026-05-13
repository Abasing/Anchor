package me.zamin.anchor.adapters;

import java.util.Collections;
import java.util.Set;
import me.zamin.anchor.api.ServiceStatus;
import me.zamin.anchor.api.regions.RegionService;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class NoOpRegionService implements RegionService {

    private final boolean permissive;

    public NoOpRegionService(boolean permissive) {
        this.permissive = permissive;
    }

    @Override
    public boolean canBuild(Player player, Location location) {
        return permissive;
    }

    @Override
    public boolean canBreak(Player player, Location location) {
        return permissive;
    }

    @Override
    public boolean canInteract(Player player, Location location) {
        return permissive;
    }

    @Override
    public boolean canPvp(Player player, Location location) {
        return permissive;
    }

    @Override
    public boolean isInRegion(Location location, String regionId) {
        return false;
    }

    @Override
    public Set<String> getRegionsAt(Location location) {
        return Collections.emptySet();
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String providerName() {
        return "Fallback";
    }

    @Override
    public ServiceStatus status() {
        return ServiceStatus.FALLBACK;
    }
}
