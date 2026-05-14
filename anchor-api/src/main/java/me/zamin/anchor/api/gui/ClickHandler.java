package me.zamin.anchor.api.gui;

import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Handles a GUI inventory click event.
 */
@FunctionalInterface
public interface ClickHandler {

    /**
     * Handles the inventory click.
     *
     * @param event non-null click event
     */
    void handle(InventoryClickEvent event);
}
