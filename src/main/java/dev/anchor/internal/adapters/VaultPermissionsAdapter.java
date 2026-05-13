package dev.anchor.internal.adapters;

import dev.anchor.core.ProviderPriority;
import dev.anchor.permissions.PermissionsService;
import dev.anchor.permissions.VaultPermissionsProvider;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class VaultPermissionsAdapter extends AbstractProviderAdapter<VaultPermissionsProvider> {

    public VaultPermissionsAdapter(Plugin plugin) {
        super("Vault Permissions", "Vault", ProviderPriority.NORMAL, PermissionsService.class, plugin);
    }

    @Override
    protected VaultPermissionsProvider createProvider() {
        RegisteredServiceProvider<Permission> registration = Bukkit.getServicesManager().getRegistration(Permission.class);
        return registration == null || registration.getProvider() == null ? null : new VaultPermissionsProvider(registration.getProvider());
    }
}
