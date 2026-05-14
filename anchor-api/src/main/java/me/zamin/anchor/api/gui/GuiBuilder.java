package me.zamin.anchor.api.gui;

import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Builder for a safe inventory GUI.
 * <p>
 * GUI operations are expected to run from a thread-safe server execution
 * context. On Folia, callers should schedule GUI opening in an entity-owned or
 * global context as appropriate.
 */
public interface GuiBuilder {

    /**
     * Sets the inventory title.
     *
     * @param title non-null title text
     * @return this builder
     */
    GuiBuilder title(String title);

    /**
     * Sets the row count.
     *
     * @param rows row count between 1 and 6
     * @return this builder
     */
    GuiBuilder rows(int rows);

    /**
     * Places a clickable item in the GUI.
     *
     * @param slot slot index within the inventory bounds
     * @param item non-null item to display
     * @param handler non-null click handler
     * @return this builder
     */
    GuiBuilder item(int slot, ItemStack item, ClickHandler handler);

    /**
     * Sets an optional close handler.
     *
     * @param closeHandler non-null close consumer
     * @return this builder
     */
    GuiBuilder onClose(Consumer<Player> closeHandler);

    /**
     * Opens the GUI for a player.
     *
     * @param player non-null player
     * @return non-null GUI session
     */
    GuiSession open(Player player);
}
