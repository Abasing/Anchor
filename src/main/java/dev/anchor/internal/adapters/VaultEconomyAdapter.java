package dev.anchor.internal.adapters;

import dev.anchor.core.ProviderPriority;
import dev.anchor.economy.EconomyService;
import dev.anchor.economy.VaultEconomyProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class VaultEconomyAdapter extends AbstractProviderAdapter<VaultEconomyProvider> {

    public VaultEconomyAdapter(Plugin plugin) {
        super("Vault Economy", "Vault", ProviderPriority.HIGH, EconomyService.class, plugin);
    }

    @Override
    protected VaultEconomyProvider createProvider() {
        RegisteredServiceProvider<Economy> registration = org.bukkit.Bukkit.getServicesManager().getRegistration(Economy.class);
        return registration == null || registration.getProvider() == null ? null : new VaultEconomyProvider(registration.getProvider());
    }
}
