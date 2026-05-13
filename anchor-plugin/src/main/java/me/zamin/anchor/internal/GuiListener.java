package me.zamin.anchor.internal;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public final class GuiListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        GuiFactoryImpl.SimpleGuiSession session = GuiRegistry.get().find(event.getInventory());
        if (session == null) {
            return;
        }
        if (session.cancelByDefault()) {
            event.setCancelled(true);
        }
        GuiFactoryImpl.GuiSlot slot = session.items().get(event.getRawSlot());
        if (slot != null) {
            slot.handler().handle(event);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        GuiFactoryImpl.SimpleGuiSession session = GuiRegistry.get().remove(event.getInventory());
        if (session != null && session.closeHandler() != null && event.getPlayer() instanceof Player player) {
            session.closeHandler().accept(player);
        }
    }
}
