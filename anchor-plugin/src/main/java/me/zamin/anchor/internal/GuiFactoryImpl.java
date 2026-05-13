package me.zamin.anchor.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import me.zamin.anchor.api.ServiceStatus;
import me.zamin.anchor.api.gui.ClickHandler;
import me.zamin.anchor.api.gui.GuiBuilder;
import me.zamin.anchor.api.gui.GuiFactory;
import me.zamin.anchor.api.gui.GuiSession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public final class GuiFactoryImpl implements GuiFactory {

    private final Plugin plugin;

    public GuiFactoryImpl(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public GuiBuilder builder() {
        return new GuiBuilder() {
            private String title = "Anchor GUI";
            private int rows = 3;
            private final Map<Integer, GuiSlot> items = new LinkedHashMap<>();
            private Consumer<Player> closeHandler;

            @Override
            public GuiBuilder title(String title) {
                this.title = title;
                return this;
            }

            @Override
            public GuiBuilder rows(int rows) {
                this.rows = rows;
                return this;
            }

            @Override
            public GuiBuilder item(int slot, ItemStack item, ClickHandler handler) {
                items.put(slot, new GuiSlot(item.clone(), handler));
                return this;
            }

            @Override
            public GuiBuilder onClose(Consumer<Player> closeHandler) {
                this.closeHandler = closeHandler;
                return this;
            }

            @Override
            public GuiSession open(Player player) {
                Inventory inventory = Bukkit.createInventory(player, rows * 9, title);
                items.forEach((slot, guiSlot) -> inventory.setItem(slot, guiSlot.item()));
                SimpleGuiSession session = new SimpleGuiSession(player.getUniqueId(), inventory, items,
                    plugin.getConfig().getBoolean("gui.cancel-clicks-by-default", true), closeHandler);
                GuiRegistry.get().register(session);
                player.openInventory(inventory);
                return session;
            }
        };
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String providerName() {
        return "AnchorGUI";
    }

    @Override
    public ServiceStatus status() {
        return ServiceStatus.AVAILABLE;
    }

    record GuiSlot(ItemStack item, ClickHandler handler) {
    }

    record SimpleGuiSession(UUID viewerId, Inventory inventory, Map<Integer, GuiSlot> items,
                            boolean cancelByDefault, Consumer<Player> closeHandler) implements GuiSession {
    }
}
