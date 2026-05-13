package dev.anchor.internal.adapters;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import dev.anchor.core.ProviderPriority;
import dev.anchor.regions.RegionService;
import dev.anchor.regions.WorldGuardRegionProvider;
import org.bukkit.plugin.Plugin;

public final class WorldGuardAdapter extends AbstractProviderAdapter<WorldGuardRegionProvider> {

    public WorldGuardAdapter(Plugin plugin) {
        super("WorldGuard", "WorldGuard", ProviderPriority.HIGH, RegionService.class, plugin);
    }

    @Override
    protected WorldGuardRegionProvider createProvider() {
        Plugin plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("WorldGuard");
        return plugin instanceof WorldGuardPlugin worldGuardPlugin ? new WorldGuardRegionProvider(worldGuardPlugin) : null;
    }
}
