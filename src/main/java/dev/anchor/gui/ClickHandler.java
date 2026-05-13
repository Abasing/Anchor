package dev.anchor.gui;

import org.bukkit.event.inventory.InventoryClickEvent;

@FunctionalInterface
public interface ClickHandler {

    void handle(InventoryClickEvent event);
}
