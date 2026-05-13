package me.zamin.anchor.adapters;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import me.zamin.anchor.api.ServiceStatus;
import me.zamin.anchor.api.regions.RegionService;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class WorldGuardRegionService implements RegionService {

    private final WorldGuardPlugin worldGuard;

    public WorldGuardRegionService(WorldGuardPlugin worldGuard) {
        this.worldGuard = Objects.requireNonNull(worldGuard, "worldGuard");
    }

    @Override
    public boolean canBuild(Player player, Location location) {
        return testFlag(player, location, Flags.BUILD);
    }

    @Override
    public boolean canBreak(Player player, Location location) {
        return testFlag(player, location, Flags.BLOCK_BREAK);
    }

    @Override
    public boolean canInteract(Player player, Location location) {
        return testFlag(player, location, Flags.INTERACT);
    }

    @Override
    public boolean canPvp(Player player, Location location) {
        return testFlag(player, location, Flags.PVP);
    }

    @Override
    public boolean isInRegion(Location location, String regionId) {
        return getRegionsAt(location).stream().anyMatch(regionId::equalsIgnoreCase);
    }

    @Override
    public Set<String> getRegionsAt(Location location) {
        ApplicableRegionSet set = getRegionSet(location);
        Set<String> ids = new LinkedHashSet<>();
        for (ProtectedRegion region : set) {
            ids.add(region.getId());
        }
        return ids;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String providerName() {
        return "WorldGuard";
    }

    @Override
    public ServiceStatus status() {
        return ServiceStatus.AVAILABLE;
    }

    private boolean testFlag(Player player, Location location, com.sk89q.worldguard.protection.flags.StateFlag flag) {
        if (player == null || location == null || location.getWorld() == null) {
            return false;
        }
        return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
            .testState(BukkitAdapter.adapt(location), worldGuard.wrapPlayer(player), flag);
    }

    private ApplicableRegionSet getRegionSet(Location location) {
        World world = Objects.requireNonNull(location.getWorld(), "location.world");
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (manager == null) {
            return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(location));
        }
        return manager.getApplicableRegions(BukkitAdapter.adapt(location).toVector().toBlockPoint());
    }
}
