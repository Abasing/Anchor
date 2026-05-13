package dev.anchor.internal.items;

import dev.anchor.core.ServiceStatus;
import dev.anchor.items.ItemService;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public final class PersistentDataItemService implements ItemService {

    private final Plugin plugin;

    public PersistentDataItemService(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    public ItemStack setString(ItemStack item, String key, String value) {
        Objects.requireNonNull(value, "value");
        return mutate(item, meta -> meta.getPersistentDataContainer().set(namespacedKey(key), PersistentDataType.STRING, value));
    }

    @Override
    public Optional<String> getString(ItemStack item, String key) {
        return read(item, key, PersistentDataType.STRING);
    }

    @Override
    public ItemStack setInt(ItemStack item, String key, int value) {
        return mutate(item, meta -> meta.getPersistentDataContainer().set(namespacedKey(key), PersistentDataType.INTEGER, value));
    }

    @Override
    public Optional<Integer> getInt(ItemStack item, String key) {
        return read(item, key, PersistentDataType.INTEGER);
    }

    @Override
    public boolean hasKey(ItemStack item, String key) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(namespacedKey(key));
    }

    @Override
    public ItemStack removeKey(ItemStack item, String key) {
        return mutate(item, meta -> meta.getPersistentDataContainer().remove(namespacedKey(key)));
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getProviderName() {
        return "PersistentDataContainer";
    }

    @Override
    public ServiceStatus getStatus() {
        return ServiceStatus.AVAILABLE;
    }

    private <T, Z> Optional<Z> read(ItemStack item, String key, PersistentDataType<T, Z> type) {
        if (item == null || !item.hasItemMeta()) {
            return Optional.empty();
        }
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return Optional.ofNullable(container.get(namespacedKey(key), type));
    }

    private ItemStack mutate(ItemStack item, java.util.function.Consumer<ItemMeta> consumer) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(consumer, "consumer");
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) {
            return clone;
        }
        consumer.accept(meta);
        clone.setItemMeta(meta);
        return clone;
    }

    private NamespacedKey namespacedKey(String key) {
        Objects.requireNonNull(key, "key");
        String normalized = key.toLowerCase(Locale.ROOT).replace(' ', '-');
        return new NamespacedKey(plugin, normalized);
    }
}
