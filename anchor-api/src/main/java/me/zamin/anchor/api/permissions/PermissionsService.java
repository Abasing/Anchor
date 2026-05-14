package me.zamin.anchor.api.permissions;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import me.zamin.anchor.api.AnchorService;
import org.bukkit.entity.Player;

/**
 * Stable abstraction over LuckPerms, Vault permissions, and Bukkit fallback
 * permission checks.
 */
public interface PermissionsService extends AnchorService {

    /**
     * Checks a permission by UUID.
     *
     * @param playerId non-null player UUID
     * @param permission non-null permission node
     * @return {@code true} when granted
     */
    boolean has(UUID playerId, String permission);

    /**
     * Checks a permission directly on a live player instance.
     *
     * @param player non-null player instance
     * @param permission non-null permission node
     * @return {@code true} when granted
     */
    boolean has(Player player, String permission);

    /**
     * Returns the known groups for a player.
     *
     * @param playerId non-null player UUID
     * @return non-null group set, possibly empty on fallback providers
     */
    Set<String> getGroups(UUID playerId);

    /**
     * Returns the primary group if the backing provider supports it.
     *
     * @param playerId non-null player UUID
     * @return optional primary group
     */
    Optional<String> getPrimaryGroup(UUID playerId);
}
