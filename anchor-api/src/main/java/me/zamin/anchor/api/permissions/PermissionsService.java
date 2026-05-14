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
     * Checks a permission by UUID in a world-specific context when the backing
     * provider supports world-aware resolution.
     * <p>
     * On providers without world-aware contexts this falls back to the global
     * permission check. Safe on Paper and Folia. No async work is performed by
     * this contract, though providers may rely on cached permission state.
     *
     * @param playerId non-null player UUID
     * @param world non-null world name
     * @param permission non-null permission node
     * @return {@code true} when granted in the requested world context
     */
    default boolean has(UUID playerId, String world, String permission) {
        return has(playerId, permission);
    }

    /**
     * Checks a permission directly on a live player instance.
     *
     * @param player non-null player instance
     * @param permission non-null permission node
     * @return {@code true} when granted
     */
    boolean has(Player player, String permission);

    /**
     * Checks a permission directly on a live player instance in a world-specific
     * context when the backing provider supports world-aware resolution.
     * <p>
     * On providers without world-aware contexts this falls back to the global
     * player permission check. Safe on Paper and Folia. No async work is
     * performed by this contract.
     *
     * @param player non-null player instance
     * @param world non-null world name
     * @param permission non-null permission node
     * @return {@code true} when granted in the requested world context
     */
    default boolean has(Player player, String world, String permission) {
        return has(player, permission);
    }

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

    /**
     * Grants a global permission to a player when the backing provider supports
     * permission mutation.
     * <p>
     * Providers that only support permission checks return an unsupported
     * result instead of throwing.
     *
     * @param playerId non-null player UUID
     * @param permission non-null permission node
     * @return non-null mutation result
     */
    default PermissionResult grant(UUID playerId, String permission) {
        return PermissionResult.unsupported(providerName(), "Permission mutation is not supported by this provider.");
    }

    /**
     * Revokes a global permission from a player when the backing provider
     * supports permission mutation.
     *
     * @param playerId non-null player UUID
     * @param permission non-null permission node
     * @return non-null mutation result
     */
    default PermissionResult revoke(UUID playerId, String permission) {
        return PermissionResult.unsupported(providerName(), "Permission mutation is not supported by this provider.");
    }

    /**
     * Grants a world-specific permission to a player when the backing provider
     * supports world-aware mutation.
     *
     * @param playerId non-null player UUID
     * @param world non-null world name
     * @param permission non-null permission node
     * @return non-null mutation result
     */
    default PermissionResult grant(UUID playerId, String world, String permission) {
        return PermissionResult.unsupported(providerName(), "World-aware permission mutation is not supported by this provider.");
    }

    /**
     * Revokes a world-specific permission from a player when the backing
     * provider supports world-aware mutation.
     *
     * @param playerId non-null player UUID
     * @param world non-null world name
     * @param permission non-null permission node
     * @return non-null mutation result
     */
    default PermissionResult revoke(UUID playerId, String world, String permission) {
        return PermissionResult.unsupported(providerName(), "World-aware permission mutation is not supported by this provider.");
    }
}
