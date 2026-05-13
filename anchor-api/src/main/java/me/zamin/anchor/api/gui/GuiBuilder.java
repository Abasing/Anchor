package me.zamin.anchor.api.gui;

import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface GuiBuilder {

    GuiBuilder title(String title);

    GuiBuilder rows(int rows);

    GuiBuilder item(int slot, ItemStack item, ClickHandler handler);

    GuiBuilder onClose(Consumer<Player> closeHandler);

    GuiSession open(Player player);
}
