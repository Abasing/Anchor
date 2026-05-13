package dev.anchor.internal.gui;

import dev.anchor.gui.GuiSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.inventory.Inventory;

public final class GuiManager {

    private static final GuiManager INSTANCE = new GuiManager();

    private final Map<Inventory, GuiSession> sessions = new ConcurrentHashMap<>();

    private GuiManager() {
    }

    public static GuiManager get() {
        return INSTANCE;
    }

    public void register(GuiSession session) {
        sessions.put(session.inventory(), session);
    }

    public GuiSession find(Inventory inventory) {
        return sessions.get(inventory);
    }

    public GuiSession remove(Inventory inventory) {
        return sessions.remove(inventory);
    }

    public void clear() {
        sessions.clear();
    }
}
