package me.zamin.anchor.internal;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import me.zamin.anchor.adapters.BukkitPermissionsService;
import me.zamin.anchor.adapters.InternalPlaceholderService;
import me.zamin.anchor.adapters.LuckPermsPermissionsService;
import me.zamin.anchor.adapters.NoOpEconomyService;
import me.zamin.anchor.adapters.NoOpRegionService;
import me.zamin.anchor.adapters.PlaceholderApiService;
import me.zamin.anchor.adapters.VaultEconomyService;
import me.zamin.anchor.adapters.VaultPermissionsService;
import me.zamin.anchor.adapters.WorldGuardRegionService;
import me.zamin.anchor.api.AnchorApi;
import me.zamin.anchor.api.AnchorPlatform;
import me.zamin.anchor.api.AnchorService;
import me.zamin.anchor.api.ServiceStatus;
import me.zamin.anchor.api.diagnostics.DiagnosticsService;
import me.zamin.anchor.api.economy.EconomyService;
import me.zamin.anchor.api.gui.GuiFactory;
import me.zamin.anchor.api.hooks.HookService;
import me.zamin.anchor.api.hooks.HookState;
import me.zamin.anchor.api.hooks.HookStatus;
import me.zamin.anchor.api.items.ItemTagService;
import me.zamin.anchor.api.permissions.PermissionsService;
import me.zamin.anchor.api.placeholders.PlaceholderService;
import me.zamin.anchor.api.regions.RegionService;
import me.zamin.anchor.api.scheduler.SchedulerService;
import me.zamin.anchor.api.services.ServiceRegistry;
import me.zamin.anchor.plugin.AnchorPlugin;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicesManager;

public final class AnchorRuntime {

    private final AnchorPlugin plugin;
    private final SimpleServiceRegistry services = new SimpleServiceRegistry();
    private final HookServiceImpl hooks = new HookServiceImpl();
    private SimpleAnchorApi api;

    public AnchorRuntime(AnchorPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        AnchorPlatform platform = PlatformDetector.detect();
        EconomyService economy = loadEconomy();
        PermissionsService permissions = loadPermissions();
        PlaceholderService placeholders = loadPlaceholders(platform);
        RegionService regions = loadRegions();
        ItemTagService items = new PersistentDataItemTagService(plugin);
        GuiFactory guis = new GuiFactoryImpl(plugin);
        SchedulerService scheduler = new BukkitAnchorScheduler(plugin);
        registerSkeletonHooks();
        DiagnosticsService diagnostics = new DiagnosticsServiceImpl(hooks, List.of(economy, permissions, placeholders, regions, items, guis, scheduler));

        services.register(EconomyService.class, economy);
        services.register(PermissionsService.class, permissions);
        services.register(PlaceholderService.class, placeholders);
        services.register(RegionService.class, regions);
        services.register(ItemTagService.class, items);
        services.register(GuiFactory.class, guis);
        services.register(SchedulerService.class, scheduler);
        services.register(HookService.class, hooks);
        services.register(DiagnosticsService.class, diagnostics);

        api = new SimpleAnchorApi(economy, permissions, placeholders, regions, items, guis, scheduler, hooks, diagnostics, services, platform);
        plugin.getServer().getPluginManager().registerEvents(new GuiListener(), plugin);

        if (plugin.getConfig().getBoolean("logging.show-hook-status-on-startup", true)) {
            for (HookStatus hook : hooks.all()) {
                plugin.getLogger().info(hook.hookName() + " -> " + hook.state() + " (" + hook.message() + ")");
            }
        }
    }

    public void disable() {
        org.bukkit.event.HandlerList.unregisterAll(plugin);
        GuiRegistry.get().clear();
    }

    public AnchorApi api() {
        return api;
    }

    private EconomyService loadEconomy() {
        if (!plugin.getConfig().getBoolean("hooks.vault", true)) {
            hooks.register(new HookStatus("Vault Economy", "Vault", HookState.DISABLED, "none", "Vault hook disabled in config."));
            return new NoOpEconomyService();
        }
        ServicesManager servicesManager = Bukkit.getServicesManager();
        Economy economy = servicesManager.load(Economy.class);
        if (economy == null || !isPluginEnabled("Vault")) {
            hooks.register(new HookStatus("Vault Economy", "Vault", HookState.MISSING, "none", "Vault economy not installed."));
            return new NoOpEconomyService();
        }
        hooks.register(new HookStatus("Vault Economy", "Vault", HookState.ACTIVE, economy.getName(), "Vault economy bridge active."));
        return new VaultEconomyService(economy);
    }

    private PermissionsService loadPermissions() {
        if (plugin.getConfig().getBoolean("hooks.luckperms", true) && isPluginEnabled("LuckPerms")) {
            LuckPerms luckPerms = Bukkit.getServicesManager().load(LuckPerms.class);
            if (luckPerms != null) {
                hooks.register(new HookStatus("LuckPerms", "LuckPerms", HookState.ACTIVE, "LuckPerms", "LuckPerms bridge active."));
                return new LuckPermsPermissionsService(luckPerms);
            }
        }
        if (plugin.getConfig().getBoolean("hooks.vault", true) && isPluginEnabled("Vault")) {
            Permission permission = Bukkit.getServicesManager().load(Permission.class);
            if (permission != null) {
                hooks.register(new HookStatus("Vault Permissions", "Vault", HookState.ACTIVE, permission.getName(), "Vault permissions bridge active."));
                return new VaultPermissionsService(permission);
            }
        }
        hooks.register(new HookStatus("Permissions Fallback", "Bukkit", HookState.FALLBACK, "Bukkit", "Using Bukkit permission fallback."));
        return new BukkitPermissionsService();
    }

    private PlaceholderService loadPlaceholders(AnchorPlatform platform) {
        InternalPlaceholderService fallback = new InternalPlaceholderService(platform.serverVersion());
        if (!plugin.getConfig().getBoolean("hooks.placeholderapi", true)) {
            hooks.register(new HookStatus("PlaceholderAPI", "PlaceholderAPI", HookState.DISABLED, "Internal", "PlaceholderAPI disabled in config."));
            return fallback;
        }
        if (!isPluginEnabled("PlaceholderAPI")) {
            hooks.register(new HookStatus("PlaceholderAPI", "PlaceholderAPI", HookState.FALLBACK, "Internal", "Using internal placeholders."));
            return fallback;
        }
        hooks.register(new HookStatus("PlaceholderAPI", "PlaceholderAPI", HookState.ACTIVE, "PlaceholderAPI", "PlaceholderAPI bridge active."));
        return new PlaceholderApiService(fallback);
    }

    private RegionService loadRegions() {
        boolean permissive = plugin.getConfig().getBoolean("regions.default-permissive", true);
        if (!plugin.getConfig().getBoolean("hooks.worldguard", true)) {
            hooks.register(new HookStatus("WorldGuard", "WorldGuard", HookState.DISABLED, "Fallback", "WorldGuard disabled in config."));
            return new NoOpRegionService(permissive);
        }
        Plugin worldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (worldGuard instanceof WorldGuardPlugin worldGuardPlugin) {
            hooks.register(new HookStatus("WorldGuard", "WorldGuard", HookState.ACTIVE, "WorldGuard", "WorldGuard region bridge active."));
            return new WorldGuardRegionService(worldGuardPlugin);
        }
        hooks.register(new HookStatus("WorldGuard", "WorldGuard", HookState.FALLBACK, "Fallback", "WorldGuard missing, using permissive fallback."));
        return new NoOpRegionService(permissive);
    }

    private boolean isPluginEnabled(String name) {
        return Bukkit.getPluginManager().isPluginEnabled(name);
    }

    private void registerSkeletonHooks() {
        registerSkeletonHook("Citizens", plugin.getConfig().getBoolean("hooks.citizens", true));
        registerSkeletonHook("ProtocolLib", plugin.getConfig().getBoolean("hooks.protocollib", true));
    }

    private void registerSkeletonHook(String pluginName, boolean enabled) {
        if (!enabled) {
            hooks.register(new HookStatus(pluginName, pluginName, HookState.DISABLED, "none", pluginName + " skeleton disabled in config."));
            return;
        }
        if (isPluginEnabled(pluginName)) {
            hooks.register(new HookStatus(pluginName, pluginName, HookState.SKELETON, pluginName, pluginName + " detected, runtime abstraction not implemented yet."));
        } else {
            hooks.register(new HookStatus(pluginName, pluginName, HookState.MISSING, "none", pluginName + " not installed; skeleton hook reserved."));
        }
    }

    private static final class SimpleAnchorApi implements AnchorApi {

        private final EconomyService economy;
        private final PermissionsService permissions;
        private final PlaceholderService placeholders;
        private final RegionService regions;
        private final ItemTagService items;
        private final GuiFactory guis;
        private final SchedulerService scheduler;
        private final HookService hooks;
        private final DiagnosticsService diagnostics;
        private final ServiceRegistry services;
        private final AnchorPlatform platform;

        private SimpleAnchorApi(EconomyService economy, PermissionsService permissions, PlaceholderService placeholders,
                                RegionService regions, ItemTagService items, GuiFactory guis, SchedulerService scheduler,
                                HookService hooks, DiagnosticsService diagnostics, ServiceRegistry services, AnchorPlatform platform) {
            this.economy = economy;
            this.permissions = permissions;
            this.placeholders = placeholders;
            this.regions = regions;
            this.items = items;
            this.guis = guis;
            this.scheduler = scheduler;
            this.hooks = hooks;
            this.diagnostics = diagnostics;
            this.services = services;
            this.platform = platform;
        }

        @Override
        public EconomyService economy() {
            return economy;
        }

        @Override
        public PermissionsService permissions() {
            return permissions;
        }

        @Override
        public PlaceholderService placeholders() {
            return placeholders;
        }

        @Override
        public RegionService regions() {
            return regions;
        }

        @Override
        public ItemTagService items() {
            return items;
        }

        @Override
        public GuiFactory guis() {
            return guis;
        }

        @Override
        public SchedulerService scheduler() {
            return scheduler;
        }

        @Override
        public HookService hooks() {
            return hooks;
        }

        @Override
        public DiagnosticsService diagnostics() {
            return diagnostics;
        }

        @Override
        public ServiceRegistry services() {
            return services;
        }

        @Override
        public AnchorPlatform platform() {
            return platform;
        }
    }

    private static final class HookServiceImpl implements HookService {

        private final List<HookStatus> hooks = new ArrayList<>();

        private void register(HookStatus hookStatus) {
            hooks.add(hookStatus);
        }

        @Override
        public Collection<HookStatus> all() {
            return List.copyOf(hooks);
        }

        @Override
        public Optional<HookStatus> find(String hookName) {
            return hooks.stream().filter(hook -> hook.hookName().equalsIgnoreCase(hookName)).findFirst();
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public String providerName() {
            return "Anchor";
        }

        @Override
        public ServiceStatus status() {
            return ServiceStatus.AVAILABLE;
        }
    }

    private static final class DiagnosticsServiceImpl implements DiagnosticsService {

        private final HookServiceImpl hookService;
        private final List<AnchorService> services;

        private DiagnosticsServiceImpl(HookServiceImpl hookService, List<AnchorService> services) {
            this.hookService = hookService;
            this.services = services;
        }

        @Override
        public me.zamin.anchor.api.diagnostics.DoctorReport doctor() {
            List<String> installed = hookService.all().stream()
                .filter(hook -> hook.state() == HookState.ACTIVE || hook.state() == HookState.FALLBACK || hook.state() == HookState.SKELETON)
                .map(HookStatus::hookName)
                .toList();
            List<String> missing = hookService.all().stream()
                .filter(hook -> hook.state() == HookState.MISSING || hook.state() == HookState.DISABLED || hook.state() == HookState.FAILED)
                .map(hook -> hook.hookName() + ": " + hook.message())
                .toList();
            List<String> unavailable = services.stream()
                .filter(service -> !service.isAvailable())
                .map(service -> service.getClass().getInterfaces()[0].getSimpleName() + " via " + service.providerName())
                .toList();
            List<String> notes = List.of(
                "Citizens and ProtocolLib are currently skeleton hooks in this foundation.",
                "Folia is not claimed as supported yet; scheduler API is future-ready but runtime is Bukkit/Paper-first.",
                "Missing optional dependencies should degrade to fallbacks, not plugin crashes."
            );
            return new me.zamin.anchor.api.diagnostics.DoctorReport(installed, missing, unavailable, notes);
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public String providerName() {
            return "Anchor";
        }

        @Override
        public ServiceStatus status() {
            return ServiceStatus.AVAILABLE;
        }
    }

    private static final class SimpleServiceRegistry implements ServiceRegistry {

        private final Map<Class<?>, AnchorService> services = new LinkedHashMap<>();

        private <T extends AnchorService> void register(Class<T> type, T service) {
            services.put(type, service);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends AnchorService> Optional<T> resolve(Class<T> serviceType) {
            return Optional.ofNullable((T) services.get(serviceType));
        }

        @Override
        public Collection<AnchorService> all() {
            return List.copyOf(services.values());
        }
    }
}
