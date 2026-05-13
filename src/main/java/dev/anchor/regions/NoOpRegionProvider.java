package dev.anchor.regions;

import dev.anchor.core.ProviderPriority;
import dev.anchor.core.ServiceStatus;
import java.util.Collections;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class NoOpRegionProvider implements RegionProvider {

    private final boolean defaultPermissive;

    public NoOpRegionProvider(boolean defaultPermissive) {
        this.defaultPermissive = defaultPermissive;
    }

    @Override
    public boolean canBuild(Player player, Location location) {
        return defaultPermissive;
    }

    @Override
    public boolean canBreak(Player player, Location location) {
        return defaultPermissive;
    }

    @Override
    public boolean canInteract(Player player, Location location) {
        return defaultPermissive;
    }

    @Override
    public boolean canPvp(Player player, Location location) {
        return defaultPermissive;
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
    public String getProviderName() {
        return "Fallback";
    }

    @Override
    public ServiceStatus getStatus() {
        return ServiceStatus.FALLBACK;
    }

    @Override
    public ProviderPriority getPriority() {
        return ProviderPriority.LOWEST;
    }
}
