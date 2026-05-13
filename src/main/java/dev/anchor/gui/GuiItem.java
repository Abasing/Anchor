package dev.anchor.gui;

import java.util.Objects;
import org.bukkit.inventory.ItemStack;

public final class GuiItem {

    private final ItemStack itemStack;
    private final ClickHandler clickHandler;

    public GuiItem(ItemStack itemStack, ClickHandler clickHandler) {
        this.itemStack = Objects.requireNonNull(itemStack, "itemStack").clone();
        this.clickHandler = Objects.requireNonNull(clickHandler, "clickHandler");
    }

    public ItemStack itemStack() {
        return itemStack.clone();
    }

    public ClickHandler clickHandler() {
        return clickHandler;
    }
}
