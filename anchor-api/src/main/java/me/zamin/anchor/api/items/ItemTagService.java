package me.zamin.anchor.api.items;

import java.util.Optional;
import me.zamin.anchor.api.AnchorService;
import org.bukkit.inventory.ItemStack;

/**
 * Stable abstraction over item metadata tagging without exposing NMS.
 * <p>
 * Current runtime implementations rely on Paper/Bukkit metadata systems such as
 * {@code PersistentDataContainer}.
 */
public interface ItemTagService extends AnchorService {

    /**
     * Stores a string tag on a cloned item stack.
     *
     * @param item non-null item to clone and mutate
     * @param key non-null tag key
     * @param value non-null tag value
     * @return cloned and tagged item stack
     */
    ItemStack setString(ItemStack item, String key, String value);

    /**
     * Reads a string tag from an item.
     *
     * @param item item to inspect, may be null
     * @param key non-null tag key
     * @return optional stored value
     */
    Optional<String> getString(ItemStack item, String key);

    /**
     * Stores an integer tag on a cloned item stack.
     *
     * @param item non-null item to clone and mutate
     * @param key non-null tag key
     * @param value integer value to store
     * @return cloned and tagged item stack
     */
    ItemStack setInt(ItemStack item, String key, int value);

    /**
     * Reads an integer tag from an item.
     *
     * @param item item to inspect, may be null
     * @param key non-null tag key
     * @return optional stored value
     */
    Optional<Integer> getInt(ItemStack item, String key);

    /**
     * Returns whether a tag key is present.
     *
     * @param item item to inspect, may be null
     * @param key non-null tag key
     * @return {@code true} if present
     */
    boolean hasKey(ItemStack item, String key);

    /**
     * Removes a tag key from a cloned item stack.
     *
     * @param item non-null item to clone and mutate
     * @param key non-null tag key
     * @return cloned and updated item stack
     */
    ItemStack removeKey(ItemStack item, String key);
}
