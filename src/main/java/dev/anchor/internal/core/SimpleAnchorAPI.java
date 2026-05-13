package dev.anchor.internal.core;

import dev.anchor.core.AnchorAPI;
import dev.anchor.core.AnchorPlatform;
import dev.anchor.core.AnchorServiceRegistry;
import dev.anchor.economy.EconomyService;
import dev.anchor.items.ItemService;
import dev.anchor.permissions.PermissionsService;
import dev.anchor.placeholders.PlaceholderService;
import dev.anchor.regions.RegionService;
import dev.anchor.scheduler.SchedulerService;
import java.util.Objects;

public final class SimpleAnchorAPI implements AnchorAPI {

    private final AnchorServiceRegistry serviceRegistry;
    private final AnchorPlatform platform;

    public SimpleAnchorAPI(AnchorServiceRegistry serviceRegistry, AnchorPlatform platform) {
        this.serviceRegistry = Objects.requireNonNull(serviceRegistry, "serviceRegistry");
        this.platform = Objects.requireNonNull(platform, "platform");
    }

    @Override
    public EconomyService economy() {
        return serviceRegistry.resolve(EconomyService.class).orElseThrow();
    }

    @Override
    public PermissionsService permissions() {
        return serviceRegistry.resolve(PermissionsService.class).orElseThrow();
    }

    @Override
    public PlaceholderService placeholders() {
        return serviceRegistry.resolve(PlaceholderService.class).orElseThrow();
    }

    @Override
    public RegionService regions() {
        return serviceRegistry.resolve(RegionService.class).orElseThrow();
    }

    @Override
    public ItemService items() {
        return serviceRegistry.resolve(ItemService.class).orElseThrow();
    }

    @Override
    public SchedulerService scheduler() {
        return serviceRegistry.resolve(SchedulerService.class).orElseThrow();
    }

    @Override
    public AnchorPlatform platform() {
        return platform;
    }

    @Override
    public AnchorServiceRegistry serviceRegistry() {
        return serviceRegistry;
    }
}
