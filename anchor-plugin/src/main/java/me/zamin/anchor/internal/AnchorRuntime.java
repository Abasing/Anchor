package me.zamin.anchor.internal;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
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
import me.zamin.anchor.plugin.AnchorPlugin;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicesManager;

public final class AnchorRuntime {

    private static final List<String> DIRECT_SCHEDULER_MARKERS = List.of(
        "BukkitScheduler",
        "runTask",
        "runTaskLater",
        "runTaskTimer",
        "runTaskAsynchronously",
        "getScheduler"
    );

    private final AnchorPlugin plugin;
    private final SimpleServiceRegistry services = new SimpleServiceRegistry();
    private final HookServiceImpl hooks = new HookServiceImpl();
    private SimpleAnchorApi api;
    private StartupTimingReport startupTiming;

    public AnchorRuntime(AnchorPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        long startupStart = System.nanoTime();
        AnchorPlatform platform = PlatformDetector.detect();

        TimedResult<EconomyService> economy = loadEconomy();
        TimedResult<PermissionsService> permissions = loadPermissions();
        TimedResult<PlaceholderService> placeholders = loadPlaceholders(platform);
        TimedResult<RegionService> regions = loadRegions();
        TimedResult<SchedulerService> scheduler = createScheduler(platform);
        ItemTagService items = new PersistentDataItemTagService(plugin);
        GuiFactory guis = new GuiFactoryImpl(plugin);

        registerSkeletonHook("Citizens", plugin.getConfig().getBoolean("hooks.citizens", true), AdapterCapability.CITIZENS);
        registerSkeletonHook("ProtocolLib", plugin.getConfig().getBoolean("hooks.protocollib", true), AdapterCapability.PROTOCOLLIB);

        List<AnchorService> coreServices = List.of(economy.value(), permissions.value(), placeholders.value(), regions.value(), items, guis, scheduler.value());
        DiagnosticsService diagnostics = new DiagnosticsServiceImpl(hooks, coreServices, scheduler.value().diagnostics());

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

        api = new SimpleAnchorApi(economy.value(), permissions.value(), placeholders.value(), regions.value(), items, guis, scheduler.value(), hooks, diagnostics, services, platform);
        plugin.getServer().getPluginManager().registerEvents(new GuiListener(), plugin);

        if (plugin.getConfig().getBoolean("logging.show-hook-status-on-startup", true)) {
            plugin.getLogger().info("Anchor startup completed in " + startupTiming.totalMillis() + "ms");
            for (HookStatus hook : hooks.all()) {
                plugin.getLogger().info(hook.hookName() + " -> " + hook.state() + " (" + hook.message() + ", " + hook.loadMillis() + "ms)");
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

    private TimedResult<EconomyService> loadEconomy() {
        long start = System.nanoTime();
        if (!plugin.getConfig().getBoolean("hooks.vault", true)) {
            hooks.register(metadata("Vault Economy", "Vault", AdapterLifecycleState.DISABLED, Set.of(AdapterCapability.ECONOMY), "none", "Vault hook disabled in config.", start));
            return new TimedResult<>(new NoOpEconomyService(), nanosToMillis(System.nanoTime() - start));
        }
        ServicesManager servicesManager = Bukkit.getServicesManager();
        Economy economy = servicesManager.load(Economy.class);
        if (economy == null || !isPluginEnabled("Vault")) {
            hooks.register(metadata("Vault Economy", "Vault", AdapterLifecycleState.MISSING, Set.of(AdapterCapability.ECONOMY), "none", "Vault economy not installed.", start));
            return new TimedResult<>(new NoOpEconomyService(), nanosToMillis(System.nanoTime() - start));
        }
        hooks.register(metadata("Vault Economy", "Vault", AdapterLifecycleState.ACTIVE, Set.of(AdapterCapability.ECONOMY), economy.getName(), "Vault economy bridge active.", start));
        return new TimedResult<>(new VaultEconomyService(economy), nanosToMillis(System.nanoTime() - start));
    }

    private TimedResult<PermissionsService> loadPermissions() {
        long start = System.nanoTime();
        if (plugin.getConfig().getBoolean("hooks.luckperms", true) && isPluginEnabled("LuckPerms")) {
            LuckPerms luckPerms = Bukkit.getServicesManager().load(LuckPerms.class);
            if (luckPerms != null) {
                hooks.register(metadata("LuckPerms", "LuckPerms", AdapterLifecycleState.ACTIVE, Set.of(AdapterCapability.PERMISSIONS), "LuckPerms", "LuckPerms bridge active.", start));
                return new TimedResult<>(new LuckPermsPermissionsService(luckPerms), nanosToMillis(System.nanoTime() - start));
            }
        }
        if (plugin.getConfig().getBoolean("hooks.vault", true) && isPluginEnabled("Vault")) {
            Permission permission = Bukkit.getServicesManager().load(Permission.class);
            if (permission != null) {
                hooks.register(metadata("Vault Permissions", "Vault", AdapterLifecycleState.FALLBACK, Set.of(AdapterCapability.PERMISSIONS), permission.getName(), "Vault permissions bridge active but lower priority than LuckPerms.", start));
                return new TimedResult<>(new VaultPermissionsService(permission), nanosToMillis(System.nanoTime() - start));
            }
        }
        hooks.register(metadata("Permissions Fallback", "Bukkit", AdapterLifecycleState.FALLBACK, Set.of(AdapterCapability.PERMISSIONS), "Bukkit", "Using Bukkit permission fallback.", start));
        return new TimedResult<>(new BukkitPermissionsService(), nanosToMillis(System.nanoTime() - start));
    }

    private TimedResult<PlaceholderService> loadPlaceholders(AnchorPlatform platform) {
        long start = System.nanoTime();
        InternalPlaceholderService fallback = new InternalPlaceholderService(platform.serverVersion());
        if (!plugin.getConfig().getBoolean("hooks.placeholderapi", true)) {
            hooks.register(metadata("PlaceholderAPI", "PlaceholderAPI", AdapterLifecycleState.DISABLED, Set.of(AdapterCapability.PLACEHOLDERS), "Internal", "PlaceholderAPI disabled in config.", start));
            return new TimedResult<>(fallback, nanosToMillis(System.nanoTime() - start));
        }
        if (!isPluginEnabled("PlaceholderAPI")) {
            hooks.register(metadata("PlaceholderAPI", "PlaceholderAPI", AdapterLifecycleState.FALLBACK, Set.of(AdapterCapability.PLACEHOLDERS), "Internal", "Using internal placeholders.", start));
            return new TimedResult<>(fallback, nanosToMillis(System.nanoTime() - start));
        }
        hooks.register(metadata("PlaceholderAPI", "PlaceholderAPI", AdapterLifecycleState.ACTIVE, Set.of(AdapterCapability.PLACEHOLDERS), "PlaceholderAPI", "PlaceholderAPI bridge active.", start));
        return new TimedResult<>(new PlaceholderApiService(fallback), nanosToMillis(System.nanoTime() - start));
    }

    private TimedResult<RegionService> loadRegions() {
        long start = System.nanoTime();
        boolean permissive = plugin.getConfig().getBoolean("regions.default-permissive", true);
        if (!plugin.getConfig().getBoolean("hooks.worldguard", true)) {
            hooks.register(metadata("WorldGuard", "WorldGuard", AdapterLifecycleState.DISABLED, Set.of(AdapterCapability.REGIONS), "Fallback", "WorldGuard disabled in config.", start));
            return new TimedResult<>(new NoOpRegionService(permissive), nanosToMillis(System.nanoTime() - start));
        }
        Plugin worldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (worldGuard instanceof WorldGuardPlugin worldGuardPlugin) {
            String version = worldGuardPlugin.getPluginMeta().getVersion();
            String message = version.startsWith("7.")
                ? "WorldGuard region bridge active."
                : "WorldGuard detected, but Anchor expects v7+ APIs.";
            hooks.register(metadata("WorldGuard", "WorldGuard", AdapterLifecycleState.ACTIVE, Set.of(AdapterCapability.REGIONS), "WorldGuard", message, start));
            return new TimedResult<>(new WorldGuardRegionService(worldGuardPlugin), nanosToMillis(System.nanoTime() - start));
        }
        hooks.register(metadata("WorldGuard", "WorldGuard", AdapterLifecycleState.FALLBACK, Set.of(AdapterCapability.REGIONS), "Fallback", "WorldGuard missing, using configured fallback.", start));
        return new TimedResult<>(new NoOpRegionService(permissive), nanosToMillis(System.nanoTime() - start));
    }

    private TimedResult<SchedulerService> createScheduler(AnchorPlatform platform) {
        long start = System.nanoTime();
        SchedulerService scheduler = platform.folia() ? new FoliaSchedulerAdapter(plugin) : new BukkitSchedulerAdapter(plugin, platform.folia());
        return new TimedResult<>(scheduler, nanosToMillis(System.nanoTime() - start));
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

    private static final class HookServiceImpl implements HookService {

        private final List<AdapterMetadata> metadata = new ArrayList<>();

        private void register(AdapterMetadata adapterMetadata) {
            metadata.add(adapterMetadata);
        }

        @Override
        public Collection<HookStatus> all() {
            return metadata.stream()
                .map(meta -> new HookStatus(meta.name(), meta.dependency(), mapState(meta.state()), meta.providerName(), meta.message(), meta.loadMillis()))
                .toList();
        }

        @Override
        public Optional<HookStatus> find(String hookName) {
            return all().stream().filter(hook -> hook.hookName().equalsIgnoreCase(hookName)).findFirst();
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

        private DiagnosticsServiceImpl(HookServiceImpl hookService, List<AnchorService> services, SchedulerDiagnostics schedulerDiagnostics) {
            this.hookService = hookService;
            this.services = services;
            this.schedulerDiagnostics = schedulerDiagnostics;
        }

        @Override
        public DoctorReport doctor() {
            List<DoctorMessage> messages = new ArrayList<>();
            if (schedulerDiagnostics.foliaDetected()) {
                messages.add(new DoctorMessage(
                    DiagnosticSeverity.WARNING,
                    "FOLIA_DETECTED",
                    "Folia runtime detected.",
                    "The server is running Folia, which uses regionized threading instead of a single universal main thread.",
                    "Use Anchor scheduler contexts such as global(), region(location), entity(entity), and async() instead of direct BukkitScheduler assumptions."
                ));
            } else {
                messages.add(new DoctorMessage(
                    DiagnosticSeverity.INFO,
                    "PAPER_OR_BUKKIT",
                    "Folia runtime not detected.",
                    "The server is using Paper/Spigot style scheduling.",
                    "Region and entity scheduler contexts will fall back to Anchor's global scheduler adapter."
                ));
            }
            for (HookStatus hook : hookService.all()) {
                if (hook.state() == HookState.MISSING || hook.state() == HookState.DISABLED || hook.state() == HookState.FAILED) {
                    messages.add(new DoctorMessage(
                        DiagnosticSeverity.WARNING,
                        "HOOK_" + hook.hookName().toUpperCase().replace(' ', '_'),
                        hook.hookName() + " is not fully available.",
                        hook.message(),
                        recommendedFixForHook(hook)
                    ));
                }
                if (hook.hookName().equalsIgnoreCase("WorldGuard") && hook.message().contains("v7+")) {
                    messages.add(new DoctorMessage(
                        DiagnosticSeverity.WARNING,
                        "WORLDGUARD_VERSION",
                        "WorldGuard version may be unsupported.",
                        "Anchor expects WorldGuard v7+ APIs for stable region integration.",
                        "Update WorldGuard to a v7+ build before relying on production region checks."
                    ));
                }
            }
            for (AnchorService service : services) {
                if (!service.isAvailable()) {
                    messages.add(new DoctorMessage(
                        DiagnosticSeverity.WARNING,
                        "SERVICE_UNAVAILABLE",
                        service.getClass().getInterfaces()[0].getSimpleName() + " is currently unavailable.",
                        "Anchor selected the " + service.providerName() + " fallback or no-op implementation.",
                        "Install the matching dependency plugin or handle the unavailable state in your plugin before using the feature."
                    ));
                }
            }
            return new DoctorReport(
                schedulerDiagnostics,
                startupTiming,
                List.copyOf(hookService.all()),
                scanPlugins(schedulerDiagnostics.foliaDetected()),
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

        private List<PluginCompatibilityReport> scanPlugins(boolean foliaDetected) {
            List<PluginCompatibilityReport> reports = new ArrayList<>();
            for (Plugin candidate : plugin.getServer().getPluginManager().getPlugins()) {
                if (candidate.getName().equalsIgnoreCase("Anchor")) {
                    continue;
                }
                List<DoctorMessage> issues = new ArrayList<>();
                boolean foliaDeclared = false;
                boolean directSchedulerUsage = false;
                try {
                    Path jarPath = pluginJar(candidate);
                    if (jarPath != null && Files.exists(jarPath)) {
                        foliaDeclared = containsPluginYamlFlag(jarPath, "folia-supported: true");
                        directSchedulerUsage = containsSchedulerMarkers(jarPath);
                    }
                } catch (IOException ex) {
                    issues.add(new DoctorMessage(
                        DiagnosticSeverity.WARNING,
                        "SCAN_FAILED",
                        "Plugin compatibility scan failed for " + candidate.getName() + ".",
                        ex.getMessage(),
                        "Rebuild the plugin jar or inspect the plugin manually if compatibility information is important."
                    ));
                }
                if (foliaDetected && !foliaDeclared) {
                    issues.add(new DoctorMessage(
                        DiagnosticSeverity.WARNING,
                        "MISSING_FOLIA_DECLARATION",
                        candidate.getName() + " is not marked folia-supported.",
                        "plugin.yml does not declare folia-supported: true.",
                        "Contact the plugin author or test carefully before production use on Folia."
                    ));
                }
                if (foliaDetected && directSchedulerUsage) {
                    issues.add(new DoctorMessage(
                        DiagnosticSeverity.WARNING,
                        "DIRECT_SCHEDULER_USAGE",
                        candidate.getName() + " may call BukkitScheduler directly.",
                        "Anchor found common direct scheduler markers in the plugin jar.",
                        "Review the plugin for Folia safety or prefer Anchor scheduler abstractions in your own code."
                    ));
                }
                reports.add(new PluginCompatibilityReport(candidate.getName(), candidate.getPluginMeta().getVersion(), foliaDeclared, directSchedulerUsage, List.copyOf(issues)));
            }
            return reports;
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

        private Path pluginJar(Plugin candidate) {
            CodeSource source = candidate.getClass().getProtectionDomain().getCodeSource();
            if (source == null || source.getLocation() == null) {
                return null;
            }
            try {
                return Path.of(source.getLocation().toURI());
            } catch (URISyntaxException ex) {
                return Path.of(source.getLocation().getPath());
            }
        }

        private boolean containsPluginYamlFlag(Path jarPath, String flag) throws IOException {
            try (ZipFile zipFile = new ZipFile(jarPath.toFile())) {
                ZipEntry entry = zipFile.getEntry("plugin.yml");
                if (entry == null) {
                    return false;
                }
                try (InputStream inputStream = zipFile.getInputStream(entry)) {
                    String yaml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    return yaml.contains(flag);
                }
            }
        }

        private boolean containsSchedulerMarkers(Path jarPath) throws IOException {
            try (ZipFile zipFile = new ZipFile(jarPath.toFile())) {
                return zipFile.stream()
                    .filter(entry -> entry.getName().endsWith(".class"))
                    .anyMatch(entry -> classContainsMarker(zipFile, entry));
            }
        }

        private boolean classContainsMarker(ZipFile zipFile, ZipEntry entry) {
            try (InputStream inputStream = zipFile.getInputStream(entry)) {
                String contents = new String(inputStream.readAllBytes(), StandardCharsets.ISO_8859_1);
                return DIRECT_SCHEDULER_MARKERS.stream().anyMatch(contents::contains);
            } catch (IOException ex) {
                return false;
            }
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
