package me.zamin.anchor.api.regions;

import java.util.Set;
import me.zamin.anchor.api.AnchorService;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Stable abstraction over region and protection checks such as WorldGuard.
 */
public interface RegionService extends AnchorService {

    /**
     * Returns whether the player can build at the location.
     *
     * @param player player context, may be null on some fallback implementations
     * @param location non-null target location
     * @return {@code true} if build is permitted, or fallback-configured result when unavailable
     */
    boolean canBuild(Player player, Location location);

    /**
     * Returns whether the player can break at the location.
     *
     * @param player player context, may be null on some fallback implementations
     * @param location non-null target location
     * @return {@code true} if break is permitted
     */
    boolean canBreak(Player player, Location location);

    /**
     * Returns whether the player can interact at the location.
     *
     * @param player player context, may be null on some fallback implementations
     * @param location non-null target location
     * @return {@code true} if interaction is permitted
     */
    boolean canInteract(Player player, Location location);

    /**
     * Returns whether PVP is permitted at the location.
     *
     * @param player player context, may be null on some fallback implementations
     * @param location non-null target location
     * @return {@code true} if PVP is permitted
     */
    boolean canPvp(Player player, Location location);

    /**
     * Returns whether the supplied region identifier is present at the location.
     *
     * @param location non-null target location
     * @param regionId non-null region identifier
     * @return {@code true} if the region is present
     */
    boolean isInRegion(Location location, String regionId);

    /**
     * Returns all region identifiers known at the location.
     *
     * @param location non-null target location
     * @return non-null region identifier set
     */
    Set<String> getRegionsAt(Location location);
}
