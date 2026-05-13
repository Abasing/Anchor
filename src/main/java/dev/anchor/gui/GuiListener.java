package dev.anchor.gui;

import dev.anchor.internal.gui.GuiManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public final class GuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        GuiSession session = GuiManager.get().find(event.getInventory());
        if (session == null) {
            return;
        }
        if (session.cancelClicksByDefault()) {
            event.setCancelled(true);
        }
        GuiItem item = session.items().get(event.getRawSlot());
        if (item != null) {
            item.clickHandler().handle(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        GuiSession session = GuiManager.get().remove(event.getInventory());
        if (session != null && event.getPlayer() instanceof Player player) {
            session.handleClose(player);
        }
    }
}
