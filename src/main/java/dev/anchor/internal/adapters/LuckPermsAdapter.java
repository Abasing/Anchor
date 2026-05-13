package dev.anchor.internal.adapters;

import dev.anchor.core.ProviderPriority;
import dev.anchor.permissions.LuckPermsPermissionsProvider;
import dev.anchor.permissions.PermissionsService;
import net.luckperms.api.LuckPerms;
import org.bukkit.plugin.Plugin;

public final class LuckPermsAdapter extends AbstractProviderAdapter<LuckPermsPermissionsProvider> {

    public LuckPermsAdapter(Plugin plugin) {
        super("LuckPerms", "LuckPerms", ProviderPriority.HIGHEST, PermissionsService.class, plugin);
    }

    @Override
    protected LuckPermsPermissionsProvider createProvider() {
        Object provider = plugin().getServer().getServicesManager().load(LuckPerms.class);
        return provider instanceof LuckPerms luckPerms ? new LuckPermsPermissionsProvider(luckPerms) : null;
    }
}
