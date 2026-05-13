package me.zamin.anchor.api;

import me.zamin.anchor.api.diagnostics.DiagnosticsService;
import me.zamin.anchor.api.economy.EconomyService;
import me.zamin.anchor.api.gui.GuiFactory;
import me.zamin.anchor.api.hooks.HookService;
import me.zamin.anchor.api.items.ItemTagService;
import me.zamin.anchor.api.permissions.PermissionsService;
import me.zamin.anchor.api.placeholders.PlaceholderService;
import me.zamin.anchor.api.regions.RegionService;
import me.zamin.anchor.api.scheduler.SchedulerService;
import me.zamin.anchor.api.services.ServiceRegistry;

/**
 * Stable public entrypoint for Anchor 1.x consumers.
 */
public interface AnchorApi {

    EconomyService economy();

    PermissionsService permissions();

    PlaceholderService placeholders();

    RegionService regions();

    ItemTagService items();

    GuiFactory guis();

    SchedulerService scheduler();

    HookService hooks();

    DiagnosticsService diagnostics();

    ServiceRegistry services();

    AnchorPlatform platform();
}
