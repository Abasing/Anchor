package me.zamin.anchor.adapters;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import me.zamin.anchor.api.ServiceStatus;
import me.zamin.anchor.api.permissions.PermissionsService;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class LuckPermsPermissionsService implements PermissionsService {

    private final LuckPerms luckPerms;

    public LuckPermsPermissionsService(LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
    }

    @Override
    public boolean has(UUID playerId, String permission) {
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) {
            Player player = Bukkit.getPlayer(playerId);
            return player != null && player.hasPermission(permission);
        }
        QueryOptions queryOptions = luckPerms.getContextManager().getQueryOptions(user)
            .orElse(luckPerms.getContextManager().getStaticQueryOptions());
        CachedPermissionData data = user.getCachedData().getPermissionData(queryOptions);
        return data.checkPermission(permission).asBoolean();
    }

    @Override
    public boolean has(Player player, String permission) {
        return player != null && has(player.getUniqueId(), permission);
    }

    @Override
    public Set<String> getGroups(UUID playerId) {
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) {
            return Collections.emptySet();
        }
        Set<String> groups = new LinkedHashSet<>();
        for (InheritanceNode node : user.getNodes(NodeType.INHERITANCE)) {
            groups.add(node.getGroupName());
        }
        return Collections.unmodifiableSet(groups);
    }

    @Override
    public Optional<String> getPrimaryGroup(UUID playerId) {
        User user = luckPerms.getUserManager().getUser(playerId);
        return user == null ? Optional.empty() : Optional.ofNullable(user.getPrimaryGroup());
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String providerName() {
        return "LuckPerms";
    }

    @Override
    public ServiceStatus status() {
        return ServiceStatus.AVAILABLE;
    }
}
