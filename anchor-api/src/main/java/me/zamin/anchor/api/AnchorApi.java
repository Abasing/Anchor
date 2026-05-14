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
 * <p>
 * All methods return non-null services. If a backing provider is unavailable,
 * the returned service remains callable and reports fallback or unavailable
 * state through {@link AnchorService#status()} and
 * {@link AnchorService#isAvailable()}.
 */
public interface AnchorApi {

    /**
     * Returns the economy abstraction.
     *
     * @return non-null economy service
     */
    EconomyService economy();

    /**
     * Returns the permissions abstraction.
     *
     * @return non-null permissions service
     */
    PermissionsService permissions();

    /**
     * Returns the placeholder abstraction.
     *
     * @return non-null placeholder service
     */
    PlaceholderService placeholders();

    /**
     * Returns the region and protection abstraction.
     *
     * @return non-null region service
     */
    RegionService regions();

    /**
     * Returns the item tag abstraction backed by platform-safe metadata APIs.
     *
     * @return non-null item tag service
     */
    ItemTagService items();

    /**
     * Returns the GUI factory abstraction.
     *
     * @return non-null GUI factory
     */
    GuiFactory guis();

    /**
     * Returns the platform-aware scheduler abstraction.
     *
     * @return non-null scheduler service
     */
    SchedulerService scheduler();

    /**
     * Returns hook and adapter status information.
     *
     * @return non-null hook service
     */
    HookService hooks();

    /**
     * Returns diagnostics and doctor reporting utilities.
     *
     * @return non-null diagnostics service
     */
    DiagnosticsService diagnostics();

    /**
     * Returns the service registry used to resolve stable Anchor services.
     *
     * @return non-null service registry
     */
    ServiceRegistry services();

    /**
     * Returns the detected server platform metadata.
     *
     * @return non-null platform descriptor
     */
    AnchorPlatform platform();
}
