package me.zamin.anchor.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.inventory.Inventory;

public final class GuiRegistry {

    private static final GuiRegistry INSTANCE = new GuiRegistry();

    private final Map<Inventory, GuiFactoryImpl.SimpleGuiSession> sessions = new ConcurrentHashMap<>();

    private GuiRegistry() {
    }

    public static GuiRegistry get() {
        return INSTANCE;
    }

    public void register(GuiFactoryImpl.SimpleGuiSession session) {
        sessions.put(session.inventory(), session);
    }

    public GuiFactoryImpl.SimpleGuiSession find(Inventory inventory) {
        return sessions.get(inventory);
    }

    public GuiFactoryImpl.SimpleGuiSession remove(Inventory inventory) {
        return sessions.remove(inventory);
    }

    public void clear() {
        sessions.clear();
    }
}
