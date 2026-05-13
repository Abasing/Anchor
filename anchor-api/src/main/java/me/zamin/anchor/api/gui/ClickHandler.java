package me.zamin.anchor.api.gui;

import org.bukkit.event.inventory.InventoryClickEvent;

@FunctionalInterface
public interface ClickHandler {

    void handle(InventoryClickEvent event);
}
