package dev.anchor.gui;

import dev.anchor.Anchor;
import dev.anchor.internal.gui.GuiManager;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class GuiBuilder {

    private String title = "Anchor GUI";
    private int rows = 3;
    private final Map<Integer, GuiItem> items = new LinkedHashMap<>();
    private Consumer<Player> closeHandler;

    GuiBuilder() {
    }

    public GuiBuilder title(String title) {
        this.title = Objects.requireNonNull(title, "title");
        return this;
    }

    public GuiBuilder rows(int rows) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("Rows must be between 1 and 6.");
        }
        this.rows = rows;
        return this;
    }

    public GuiBuilder item(int slot, ItemStack item, ClickHandler handler) {
        if (slot < 0 || slot >= rows * 9) {
            throw new IllegalArgumentException("Slot must be within the inventory size.");
        }
        items.put(slot, new GuiItem(item, handler));
        return this;
    }

    public GuiBuilder onClose(Consumer<Player> closeHandler) {
        this.closeHandler = closeHandler;
        return this;
    }

    public GuiSession open(Player player) {
        Objects.requireNonNull(player, "player");
        Inventory inventory = Bukkit.createInventory(player, rows * 9, title);
        items.forEach((slot, guiItem) -> inventory.setItem(slot, guiItem.itemStack()));
        GuiSession session = new GuiSession(player.getUniqueId(), inventory, items,
            Anchor.plugin().getConfig().getBoolean("gui.cancel-clicks-by-default", true), closeHandler);
        GuiManager.get().register(session);
        player.openInventory(inventory);
        return session;
    }
}
