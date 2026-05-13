package dev.anchor.internal.core;

import dev.anchor.adapters.Adapter;
import dev.anchor.core.AnchorAPI;
import dev.anchor.core.AnchorLogger;
import dev.anchor.core.AnchorPlatform;
import dev.anchor.core.AnchorProvider;
import dev.anchor.core.AnchorService;
import dev.anchor.core.ProviderPriority;
import dev.anchor.economy.EconomyProvider;
import dev.anchor.economy.EconomyService;
import dev.anchor.economy.NoOpEconomyProvider;
import dev.anchor.gui.GuiListener;
import dev.anchor.internal.adapters.LuckPermsAdapter;
import dev.anchor.internal.adapters.PlaceholderApiAdapter;
import dev.anchor.internal.adapters.SimpleAdapterManager;
import dev.anchor.internal.adapters.VaultEconomyAdapter;
import dev.anchor.internal.adapters.VaultPermissionsAdapter;
import dev.anchor.internal.adapters.WorldGuardAdapter;
import dev.anchor.internal.gui.GuiManager;
import dev.anchor.items.ItemService;
import dev.anchor.permissions.BukkitPermissionsProvider;
import dev.anchor.permissions.PermissionsProvider;
import dev.anchor.permissions.PermissionsService;
import dev.anchor.placeholders.InternalPlaceholderProvider;
import dev.anchor.placeholders.PlaceholderProvider;
import dev.anchor.placeholders.PlaceholderService;
import dev.anchor.regions.NoOpRegionProvider;
import dev.anchor.regions.RegionProvider;
import dev.anchor.regions.RegionService;
import dev.anchor.scheduler.BukkitSchedulerService;
import dev.anchor.scheduler.SchedulerService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;

public final class AnchorBootstrap {

    private final dev.anchor.AnchorPlugin plugin;
    private final AnchorLogger logger;
    private final SimpleAnchorServiceRegistry serviceRegistry = new SimpleAnchorServiceRegistry();
    private final SimpleAdapterManager adapterManager;
    private final AnchorPlatform platform;
    private AnchorAPI api;

    public AnchorBootstrap(dev.anchor.AnchorPlugin plugin, AnchorLogger logger, AnchorPlatform platform) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.platform = Objects.requireNonNull(platform, "platform");
        this.adapterManager = new SimpleAdapterManager(logger);
    }

    public void enable() {
        registerCoreServices();
        registerAdapters();
        adapterManager.enableAll();
        bindService(EconomyService.class, selectEconomyProvider());
        bindService(PermissionsService.class, selectPermissionsProvider());
        bindService(PlaceholderService.class, selectPlaceholderProvider());
        bindService(RegionService.class, selectRegionProvider());
        api = new SimpleAnchorAPI(serviceRegistry, platform);
        registerListeners();
        if (plugin.getConfig().getBoolean("logging.show-hook-status-on-startup", true)) {
            logHookStatus();
        }
    }

    public void disable() {
        HandlerList.unregisterAll(plugin);
        adapterManager.disableAll();
        GuiManager.get().clear();
    }

    public AnchorAPI api() {
        return api;
    }

    public AnchorPlatform platform() {
        return platform;
    }

    public SimpleAnchorServiceRegistry serviceRegistry() {
        return serviceRegistry;
    }

    public SimpleAdapterManager adapterManager() {
        return adapterManager;
    }

    public AnchorLogger logger() {
        return logger;
    }

    public void logHookStatus() {
        logger.info("Anchor hooks:");
        for (Adapter adapter : adapterManager.adapters()) {
            logger.info(" - " + adapter.getName() + " [" + adapter.getStatus() + "]");
        }
        logger.info("Selected providers: economy=" + api.economy().getProviderName()
            + ", permissions=" + api.permissions().getProviderName()
            + ", placeholders=" + api.placeholders().getProviderName()
            + ", regions=" + api.regions().getProviderName()
            + ", items=" + api.items().getProviderName()
            + ", scheduler=" + api.scheduler().getProviderName());
    }

    private void registerCoreServices() {
        bindService(ItemService.class, new dev.anchor.internal.items.PersistentDataItemService(plugin));
        bindService(SchedulerService.class, new BukkitSchedulerService(plugin));
    }

    private void registerAdapters() {
        if (plugin.getConfig().getBoolean("hooks.vault", true)) {
            adapterManager.register(new VaultEconomyAdapter(plugin));
            adapterManager.register(new VaultPermissionsAdapter(plugin));
        }
        if (plugin.getConfig().getBoolean("hooks.luckperms", true)) {
            adapterManager.register(new LuckPermsAdapter(plugin));
        }
        InternalPlaceholderProvider internalPlaceholderProvider = new InternalPlaceholderProvider(platform.serverVersion());
        if (plugin.getConfig().getBoolean("hooks.placeholderapi", true)) {
            adapterManager.register(new PlaceholderApiAdapter(plugin, internalPlaceholderProvider));
        }
        if (plugin.getConfig().getBoolean("hooks.worldguard", true)) {
            adapterManager.register(new WorldGuardAdapter(plugin));
        }
        bindService(PlaceholderService.class, internalPlaceholderProvider);
    }

    private EconomyService selectEconomyProvider() {
        return selectProvider(EconomyService.class, new NoOpEconomyProvider(), EconomyProvider.class);
    }

    private PermissionsService selectPermissionsProvider() {
        return selectProvider(PermissionsService.class, new BukkitPermissionsProvider(), PermissionsProvider.class);
    }

    private PlaceholderService selectPlaceholderProvider() {
        PlaceholderService fallback = serviceRegistry.resolve(PlaceholderService.class).orElseThrow();
        return selectProvider(PlaceholderService.class, fallback, PlaceholderProvider.class);
    }

    private RegionService selectRegionProvider() {
        boolean permissive = plugin.getConfig().getBoolean("regions.default-permissive", true);
        return selectProvider(RegionService.class, new NoOpRegionProvider(permissive), RegionProvider.class);
    }

    @SuppressWarnings("unchecked")
    private <T extends AnchorService, P extends AnchorProvider> T selectProvider(Class<T> serviceType, T fallback, Class<P> providerType) {
        List<P> candidates = new ArrayList<>();
        for (Adapter adapter : adapterManager.adapters()) {
            Optional<Class<? extends AnchorService>> adapterServiceType = adapter.getServiceType();
            if (adapterServiceType.isPresent() && adapterServiceType.get().equals(serviceType)) {
                adapter.getProvider()
                    .filter(providerType::isInstance)
                    .map(providerType::cast)
                    .ifPresent(candidates::add);
            }
        }
        return candidates.stream()
            .max(Comparator.comparingInt(provider -> provider.getPriority().weight()))
            .map(serviceType::cast)
            .orElse(fallback);
    }

    private <T extends AnchorService> void bindService(Class<T> type, T service) {
        serviceRegistry.register(type, service);
    }

    private void registerListeners() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvents(new GuiListener(), plugin);
    }
}
