package me.zamin.anchor.internal;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import me.zamin.anchor.api.diagnostics.DiagnosticSeverity;
import me.zamin.anchor.api.diagnostics.DiagnosticsService;
import me.zamin.anchor.api.diagnostics.DoctorMessage;
import me.zamin.anchor.api.diagnostics.DoctorReport;
import me.zamin.anchor.api.diagnostics.PluginCompatibilityReport;
import me.zamin.anchor.api.diagnostics.StartupTimingReport;
import me.zamin.anchor.api.economy.EconomyService;
import me.zamin.anchor.api.gui.GuiFactory;
import me.zamin.anchor.api.hooks.HookService;
import me.zamin.anchor.api.hooks.HookState;
import me.zamin.anchor.api.hooks.HookStatus;
import me.zamin.anchor.api.items.ItemTagService;
import me.zamin.anchor.api.permissions.PermissionsService;
import me.zamin.anchor.api.placeholders.PlaceholderService;
import me.zamin.anchor.api.regions.RegionService;
import me.zamin.anchor.api.scheduler.SchedulerDiagnostics;
import me.zamin.anchor.api.scheduler.SchedulerService;
import me.zamin.anchor.api.services.ServiceRegistry;
import me.zamin.anchor.internal.adapters.meta.AdapterCapability;
import me.zamin.anchor.internal.adapters.meta.AdapterLifecycleState;
import me.zamin.anchor.internal.adapters.meta.AdapterMetadata;
import me.zamin.anchor.internal.compat.CompatibilityScanner;
import me.zamin.anchor.internal.metrics.AnchorMetrics;
import me.zamin.anchor.internal.metrics.MetricsSnapshot;
import me.zamin.anchor.internal.validation.RuntimeValidator;
import me.zamin.anchor.internal.validation.ValidationIssue;
import me.zamin.anchor.internal.validation.ValidationReport;
import me.zamin.anchor.internal.validation.ValidationSeverity;
import me.zamin.anchor.plugin.AnchorPlugin;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicesManager;

public final class AnchorRuntime {

    private final AnchorPlugin plugin;
    private final AnchorMetrics metrics = new AnchorMetrics();
    private final CompatibilityScanner compatibilityScanner;
    private final RuntimeValidator runtimeValidator = new RuntimeValidator(metrics);
    private final SimpleServiceRegistry services = new SimpleServiceRegistry();
    private final HookServiceImpl hooks = new HookServiceImpl();
    private SimpleAnchorApi api;
    private StartupTimingReport startupTiming;
    private ValidationReport validationReport = new ValidationReport(System.currentTimeMillis(), List.of());

    public AnchorRuntime(AnchorPlugin plugin) {
        this.plugin = plugin;
        this.compatibilityScanner = new CompatibilityScanner(plugin);
    }

    public void enable() {
        long startupStart = System.nanoTime();
        AnchorPlatform platform = PlatformDetector.detect();

        TimedResult<SchedulerService> scheduler = createScheduler(platform);
        TimedResult<EconomyService> economy = loadEconomy();
        TimedResult<PermissionsService> permissions = loadPermissions(scheduler.value());
        TimedResult<PlaceholderService> placeholders = loadPlaceholders(platform);
        TimedResult<RegionService> regions = loadRegions();
        ItemTagService items = new PersistentDataItemTagService(plugin);
        GuiFactory guis = new GuiFactoryImpl(plugin);

        registerSkeletonHook("Citizens", plugin.getConfig().getBoolean("hooks.citizens", true), AdapterCapability.CITIZENS);
        registerSkeletonHook("ProtocolLib", plugin.getConfig().getBoolean("hooks.protocollib", true), AdapterCapability.PROTOCOLLIB);

        List<AnchorService> coreServices = List.of(economy.value(), permissions.value(), placeholders.value(), regions.value(), items, guis, scheduler.value());
        DiagnosticsService diagnostics = new DiagnosticsServiceImpl(
            hooks,
            coreServices,
            scheduler.value().diagnostics(),
            economy.value(),
            permissions.value(),
            placeholders.value(),
            regions.value(),
            scheduler.value()
        );

        services.register(EconomyService.class, economy.value());
        services.register(PermissionsService.class, permissions.value());
        services.register(PlaceholderService.class, placeholders.value());
        services.register(RegionService.class, regions.value());
        services.register(ItemTagService.class, items);
        services.register(GuiFactory.class, guis);
        services.register(SchedulerService.class, scheduler.value());
        services.register(HookService.class, hooks);
        services.register(DiagnosticsService.class, diagnostics);

        long totalMillis = nanosToMillis(System.nanoTime() - startupStart);
        long hookMillis = economy.elapsedMillis() + permissions.elapsedMillis() + placeholders.elapsedMillis() + regions.elapsedMillis();
        startupTiming = new StartupTimingReport(totalMillis, scheduler.elapsedMillis(), hookMillis);
        metrics.recordTiming("startup.total", System.nanoTime() - startupStart);

        api = new SimpleAnchorApi(economy.value(), permissions.value(), placeholders.value(), regions.value(), items, guis, scheduler.value(), hooks, diagnostics, services, platform);
        plugin.getServer().getPluginManager().registerEvents(new GuiListener(), plugin);

        validationReport = runtimeValidator.validate(
            economy.value(),
            permissions.value(),
            placeholders.value(),
            regions.value(),
            scheduler.value(),
            hooks.all(),
            compatibilityScanner.scanPlugins(platform.folia())
        );

        if (plugin.getConfig().getBoolean("logging.show-hook-status-on-startup", true)) {
            plugin.getLogger().info("Anchor startup completed in " + startupTiming.totalMillis() + "ms");
            for (HookStatus hook : hooks.all()) {
                plugin.getLogger().info(hook.hookName() + " -> " + hook.state() + " (" + hook.message() + ", " + hook.loadMillis() + "ms)");
            }
            for (ValidationIssue issue : validationReport.issues()) {
                if (issue.severity() == ValidationSeverity.WARNING || issue.severity() == ValidationSeverity.ERROR) {
                    plugin.getLogger().warning(issue.category() + ": " + issue.problem() + " | Cause: " + issue.cause() + " | Fix: " + issue.recommendedFix());
                }
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

    public ValidationReport validationReport() {
        return validationReport;
    }

    public MetricsSnapshot metricsSnapshot() {
        return metrics.snapshot();
    }

    public void recordCommandTiming(String commandName, long nanos) {
        metrics.recordTiming("command." + commandName, nanos);
    }

    private TimedResult<EconomyService> loadEconomy() {
        long start = System.nanoTime();
        if (!plugin.getConfig().getBoolean("hooks.vault", true)) {
            hooks.register(metadata("Vault Economy", "Vault", AdapterLifecycleState.DISABLED, Set.of(AdapterCapability.ECONOMY), "none", "Vault hook disabled in config.", start));
            return timed("adapter.load.vaultEconomy", new NoOpEconomyService(), start);
        }
        ServicesManager servicesManager = Bukkit.getServicesManager();
        Economy economy = servicesManager.load(Economy.class);
        if (economy == null || !isPluginEnabled("Vault")) {
            hooks.register(metadata("Vault Economy", "Vault", AdapterLifecycleState.MISSING, Set.of(AdapterCapability.ECONOMY), "none", "Vault economy not installed.", start));
            return timed("adapter.load.vaultEconomy", new NoOpEconomyService(), start);
        }
        hooks.register(metadata("Vault Economy", "Vault", AdapterLifecycleState.ACTIVE, Set.of(AdapterCapability.ECONOMY), economy.getName(), "Vault economy bridge active.", start));
        return timed("adapter.load.vaultEconomy", new VaultEconomyService(economy), start);
    }

    private TimedResult<PermissionsService> loadPermissions(SchedulerService scheduler) {
        long start = System.nanoTime();
        if (plugin.getConfig().getBoolean("hooks.luckperms", true) && isPluginEnabled("LuckPerms")) {
            LuckPerms luckPerms = Bukkit.getServicesManager().load(LuckPerms.class);
            if (luckPerms != null) {
                hooks.register(metadata("LuckPerms", "LuckPerms", AdapterLifecycleState.ACTIVE, Set.of(AdapterCapability.PERMISSIONS), "LuckPerms", "LuckPerms bridge active.", start));
                return timed("adapter.load.luckPerms", new AsyncPermissionsServiceDecorator(new LuckPermsPermissionsService(luckPerms), scheduler, true), start);
            }
        }
        if (plugin.getConfig().getBoolean("hooks.vault", true) && isPluginEnabled("Vault")) {
            Permission permission = Bukkit.getServicesManager().load(Permission.class);
            if (permission != null) {
                hooks.register(metadata("Vault Permissions", "Vault", AdapterLifecycleState.FALLBACK, Set.of(AdapterCapability.PERMISSIONS), permission.getName(), "Vault permissions bridge active but lower priority than LuckPerms.", start));
                return timed("adapter.load.vaultPermissions", new AsyncPermissionsServiceDecorator(new VaultPermissionsService(permission), scheduler, false), start);
            }
        }
        hooks.register(metadata("Permissions Fallback", "Bukkit", AdapterLifecycleState.FALLBACK, Set.of(AdapterCapability.PERMISSIONS), "Bukkit", "Using Bukkit permission fallback.", start));
        return timed("adapter.load.bukkitPermissions", new AsyncPermissionsServiceDecorator(new BukkitPermissionsService(), scheduler, false), start);
    }

    private TimedResult<PlaceholderService> loadPlaceholders(AnchorPlatform platform) {
        long start = System.nanoTime();
        InternalPlaceholderService fallback = new InternalPlaceholderService(platform.serverVersion());
        if (!plugin.getConfig().getBoolean("hooks.placeholderapi", true)) {
            hooks.register(metadata("PlaceholderAPI", "PlaceholderAPI", AdapterLifecycleState.DISABLED, Set.of(AdapterCapability.PLACEHOLDERS), "Internal", "PlaceholderAPI disabled in config.", start));
            return timed("adapter.load.placeholderApi", fallback, start);
        }
        if (!isPluginEnabled("PlaceholderAPI")) {
            hooks.register(metadata("PlaceholderAPI", "PlaceholderAPI", AdapterLifecycleState.FALLBACK, Set.of(AdapterCapability.PLACEHOLDERS), "Internal", "Using internal placeholders.", start));
            return timed("adapter.load.placeholderApi", fallback, start);
        }
        hooks.register(metadata("PlaceholderAPI", "PlaceholderAPI", AdapterLifecycleState.ACTIVE, Set.of(AdapterCapability.PLACEHOLDERS), "PlaceholderAPI", "PlaceholderAPI bridge active.", start));
        return timed("adapter.load.placeholderApi", new PlaceholderApiService(fallback), start);
    }

    private TimedResult<RegionService> loadRegions() {
        long start = System.nanoTime();
        boolean permissive = plugin.getConfig().getBoolean("regions.default-permissive", true);
        if (!plugin.getConfig().getBoolean("hooks.worldguard", true)) {
            hooks.register(metadata("WorldGuard", "WorldGuard", AdapterLifecycleState.DISABLED, Set.of(AdapterCapability.REGIONS), "Fallback", "WorldGuard disabled in config.", start));
            return timed("adapter.load.worldGuard", new NoOpRegionService(permissive), start);
        }
        Plugin worldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (worldGuard instanceof WorldGuardPlugin worldGuardPlugin) {
            String version = worldGuardPlugin.getPluginMeta().getVersion();
            String message = version.startsWith("7.")
                ? "WorldGuard region bridge active."
                : "WorldGuard detected, but Anchor expects v7+ APIs.";
            hooks.register(metadata("WorldGuard", "WorldGuard", AdapterLifecycleState.ACTIVE, Set.of(AdapterCapability.REGIONS), "WorldGuard", message, start));
            return timed("adapter.load.worldGuard", new WorldGuardRegionService(worldGuardPlugin), start);
        }
        hooks.register(metadata("WorldGuard", "WorldGuard", AdapterLifecycleState.FALLBACK, Set.of(AdapterCapability.REGIONS), "Fallback", "WorldGuard missing, using configured fallback.", start));
        return timed("adapter.load.worldGuard", new NoOpRegionService(permissive), start);
    }

    private TimedResult<SchedulerService> createScheduler(AnchorPlatform platform) {
        long start = System.nanoTime();
        SchedulerService scheduler = platform.folia() ? new FoliaSchedulerAdapter(plugin, metrics) : new BukkitSchedulerAdapter(plugin, platform.folia(), metrics);
        return timed("adapter.load.scheduler", scheduler, start);
    }

    private boolean isPluginEnabled(String name) {
        return Bukkit.getPluginManager().isPluginEnabled(name);
    }

    private void registerSkeletonHook(String pluginName, boolean enabled, AdapterCapability capability) {
        long start = System.nanoTime();
        if (!enabled) {
            hooks.register(metadata(pluginName, pluginName, AdapterLifecycleState.DISABLED, Set.of(capability), "none", pluginName + " skeleton disabled in config.", start));
            return;
        }
        if (isPluginEnabled(pluginName)) {
            hooks.register(metadata(pluginName, pluginName, AdapterLifecycleState.SKELETON, Set.of(capability), pluginName, pluginName + " detected, runtime abstraction not implemented yet.", start));
        } else {
            hooks.register(metadata(pluginName, pluginName, AdapterLifecycleState.MISSING, Set.of(capability), "none", pluginName + " not installed; skeleton hook reserved.", start));
        }
    }

    private AdapterMetadata metadata(String name, String dependency, AdapterLifecycleState state, Set<AdapterCapability> capabilities,
                                     String providerName, String message, long startNanos) {
        return new AdapterMetadata(name, dependency, state, capabilities, providerName, message, nanosToMillis(System.nanoTime() - startNanos));
    }

    private static HookState mapState(AdapterLifecycleState state) {
        return switch (state) {
            case ACTIVE -> HookState.ACTIVE;
            case FALLBACK -> HookState.FALLBACK;
            case MISSING -> HookState.MISSING;
            case DISABLED -> HookState.DISABLED;
            case FAILED -> HookState.FAILED;
            case SKELETON -> HookState.SKELETON;
        };
    }

    private static long nanosToMillis(long nanos) {
        return nanos / 1_000_000L;
    }

    private <T> TimedResult<T> timed(String metricKey, T value, long startNanos) {
        long elapsedNanos = System.nanoTime() - startNanos;
        metrics.recordTiming(metricKey, elapsedNanos);
        return new TimedResult<>(value, nanosToMillis(elapsedNanos));
    }

    private record TimedResult<T>(T value, long elapsedMillis) {
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
            this.economy = Objects.requireNonNull(economy, "economy");
            this.permissions = Objects.requireNonNull(permissions, "permissions");
            this.placeholders = Objects.requireNonNull(placeholders, "placeholders");
            this.regions = Objects.requireNonNull(regions, "regions");
            this.items = Objects.requireNonNull(items, "items");
            this.guis = Objects.requireNonNull(guis, "guis");
            this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
            this.hooks = Objects.requireNonNull(hooks, "hooks");
            this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics");
            this.services = Objects.requireNonNull(services, "services");
            this.platform = Objects.requireNonNull(platform, "platform");
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

    private final class HookServiceImpl implements HookService {

        private final List<AdapterMetadata> metadata = new ArrayList<>();

        private void register(AdapterMetadata adapterMetadata) {
            metadata.add(adapterMetadata);
        }

        @Override
        public Collection<HookStatus> all() {
            long start = System.nanoTime();
            Collection<HookStatus> results = metadata.stream()
                .map(meta -> new HookStatus(meta.name(), meta.dependency(), mapState(meta.state()), meta.providerName(), meta.message(), meta.loadMillis()))
                .toList();
            metrics.recordTiming("hooks.lookup.all", System.nanoTime() - start);
            return results;
        }

        @Override
        public Optional<HookStatus> find(String hookName) {
            long start = System.nanoTime();
            Optional<HookStatus> result = all().stream().filter(hook -> hook.hookName().equalsIgnoreCase(hookName)).findFirst();
            metrics.recordTiming("hooks.lookup.find", System.nanoTime() - start);
            return result;
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

    private final class DiagnosticsServiceImpl implements DiagnosticsService {

        private final HookServiceImpl hookService;
        private final List<AnchorService> services;
        private final SchedulerDiagnostics schedulerDiagnostics;
        private final EconomyService economy;
        private final PermissionsService permissions;
        private final PlaceholderService placeholders;
        private final RegionService regions;
        private final SchedulerService scheduler;

        private DiagnosticsServiceImpl(HookServiceImpl hookService, List<AnchorService> services, SchedulerDiagnostics schedulerDiagnostics,
                                       EconomyService economy, PermissionsService permissions, PlaceholderService placeholders,
                                       RegionService regions, SchedulerService scheduler) {
            this.hookService = hookService;
            this.services = services;
            this.schedulerDiagnostics = schedulerDiagnostics;
            this.economy = economy;
            this.permissions = permissions;
            this.placeholders = placeholders;
            this.regions = regions;
            this.scheduler = scheduler;
        }

        @Override
        public DoctorReport doctor() {
            long start = System.nanoTime();
            List<HookStatus> hookStatuses = List.copyOf(hookService.all());
            List<PluginCompatibilityReport> pluginReports = compatibilityScanner.scanPlugins(schedulerDiagnostics.foliaDetected());
            ValidationReport report = runtimeValidator.validate(economy, permissions, placeholders, regions, scheduler, hookStatuses, pluginReports);
            List<DoctorMessage> messages = new ArrayList<>();
            for (ValidationIssue issue : report.issues()) {
                messages.add(new DoctorMessage(
                    mapSeverity(issue.severity()),
                    issue.category().name(),
                    issue.problem(),
                    issue.cause(),
                    issue.recommendedFix()
                ));
            }
            for (HookStatus hook : hookStatuses) {
                if (hook.state() == HookState.MISSING || hook.state() == HookState.DISABLED || hook.state() == HookState.FAILED) {
                    messages.add(new DoctorMessage(
                        DiagnosticSeverity.WARNING,
                        "HOOK_" + hook.hookName().toUpperCase().replace(' ', '_'),
                        hook.hookName() + " is not fully available.",
                        hook.message(),
                        recommendedFixForHook(hook)
                    ));
                }
            }
            metrics.recordTiming("doctor.scan", System.nanoTime() - start);
            return new DoctorReport(
                schedulerDiagnostics,
                startupTiming,
                hookStatuses,
                pluginReports,
                List.copyOf(messages)
            );
        }

        @Override
        public SchedulerDiagnostics schedulerDiagnostics() {
            return schedulerDiagnostics;
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

        private String recommendedFixForHook(HookStatus hook) {
            if (hook.hookName().contains("Vault")) {
                return "Install Vault and a compatible economy or permissions provider, or keep handling unavailable economy and permission features gracefully.";
            }
            if (hook.hookName().equalsIgnoreCase("PlaceholderAPI")) {
                return "Install PlaceholderAPI for third-party placeholder support, or continue using Anchor's internal placeholders only.";
            }
            if (hook.hookName().equalsIgnoreCase("WorldGuard")) {
                return "Install or update WorldGuard, or confirm the configured fallback behavior is acceptable for this server.";
            }
            if (hook.hookName().equalsIgnoreCase("Citizens") || hook.hookName().equalsIgnoreCase("ProtocolLib")) {
                return "No runtime abstraction exists yet. Treat this hook as reserved architecture, not a ready integration.";
            }
            return "Install the dependency plugin or keep using the current fallback behavior.";
        }

        private DiagnosticSeverity mapSeverity(ValidationSeverity severity) {
            return switch (severity) {
                case INFO -> DiagnosticSeverity.INFO;
                case WARNING -> DiagnosticSeverity.WARNING;
                case ERROR -> DiagnosticSeverity.ERROR;
            };
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
