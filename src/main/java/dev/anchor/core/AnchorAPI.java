package dev.anchor.core;

import dev.anchor.economy.EconomyService;
import dev.anchor.items.ItemService;
import dev.anchor.permissions.PermissionsService;
import dev.anchor.placeholders.PlaceholderService;
import dev.anchor.regions.RegionService;
import dev.anchor.scheduler.SchedulerService;

public interface AnchorAPI {

    EconomyService economy();

    PermissionsService permissions();

    PlaceholderService placeholders();

    RegionService regions();

    ItemService items();

    SchedulerService scheduler();

    AnchorPlatform platform();

    AnchorServiceRegistry serviceRegistry();
}
