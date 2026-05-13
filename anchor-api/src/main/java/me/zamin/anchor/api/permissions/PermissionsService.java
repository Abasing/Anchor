package me.zamin.anchor.api.permissions;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import me.zamin.anchor.api.AnchorService;
import org.bukkit.entity.Player;

public interface PermissionsService extends AnchorService {

    boolean has(UUID playerId, String permission);

    boolean has(Player player, String permission);

    Set<String> getGroups(UUID playerId);

    Optional<String> getPrimaryGroup(UUID playerId);
}
