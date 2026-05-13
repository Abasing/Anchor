package me.zamin.anchor.internal;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import me.zamin.anchor.api.ServiceStatus;
import me.zamin.anchor.api.items.ItemTagService;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public final class PersistentDataItemTagService implements ItemTagService {

    private final Plugin plugin;

    public PersistentDataItemTagService(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public ItemStack setString(ItemStack item, String key, String value) {
        return mutate(item, meta -> meta.getPersistentDataContainer().set(key(key), PersistentDataType.STRING, value));
    }

    @Override
    public Optional<String> getString(ItemStack item, String key) {
        return read(item, key, PersistentDataType.STRING);
    }

    @Override
    public ItemStack setInt(ItemStack item, String key, int value) {
        return mutate(item, meta -> meta.getPersistentDataContainer().set(key(key), PersistentDataType.INTEGER, value));
    }

    @Override
    public Optional<Integer> getInt(ItemStack item, String key) {
        return read(item, key, PersistentDataType.INTEGER);
    }

    @Override
    public boolean hasKey(ItemStack item, String key) {
        return item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(key(key));
    }

    @Override
    public ItemStack removeKey(ItemStack item, String key) {
        return mutate(item, meta -> meta.getPersistentDataContainer().remove(key(key)));
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String providerName() {
        return "PersistentDataContainer";
    }

    @Override
    public ServiceStatus status() {
        return ServiceStatus.AVAILABLE;
    }

    private NamespacedKey key(String raw) {
        return new NamespacedKey(plugin, raw.toLowerCase(Locale.ROOT).replace(' ', '-'));
    }

    private <P, C> Optional<C> read(ItemStack item, String key, PersistentDataType<P, C> type) {
        if (item == null || !item.hasItemMeta()) {
            return Optional.empty();
        }
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return Optional.ofNullable(container.get(key(key), type));
    }

    private ItemStack mutate(ItemStack item, java.util.function.Consumer<ItemMeta> consumer) {
        Objects.requireNonNull(item, "item");
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta != null) {
            consumer.accept(meta);
            clone.setItemMeta(meta);
        }
        return clone;
    }
}
