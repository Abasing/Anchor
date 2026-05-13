package dev.anchor.gui;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class GuiSession {

    private final UUID viewerId;
    private final Inventory inventory;
    private final Map<Integer, GuiItem> items;
    private final boolean cancelClicksByDefault;
    private final Consumer<Player> closeHandler;

    public GuiSession(UUID viewerId, Inventory inventory, Map<Integer, GuiItem> items,
                      boolean cancelClicksByDefault, Consumer<Player> closeHandler) {
        this.viewerId = Objects.requireNonNull(viewerId, "viewerId");
        this.inventory = Objects.requireNonNull(inventory, "inventory");
        this.items = Map.copyOf(items);
        this.cancelClicksByDefault = cancelClicksByDefault;
        this.closeHandler = closeHandler;
    }

    public UUID viewerId() {
        return viewerId;
    }

    public Inventory inventory() {
        return inventory;
    }

    public Map<Integer, GuiItem> items() {
        return Collections.unmodifiableMap(items);
    }

    public boolean cancelClicksByDefault() {
        return cancelClicksByDefault;
    }

    public void handleClose(Player player) {
        if (closeHandler != null && player != null) {
            closeHandler.accept(player);
        }
    }
}
