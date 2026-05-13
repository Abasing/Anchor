package dev.anchor.items;

import dev.anchor.core.AnchorService;
import java.util.Optional;
import org.bukkit.inventory.ItemStack;

public interface ItemService extends AnchorService {

    ItemStack setString(ItemStack item, String key, String value);

    Optional<String> getString(ItemStack item, String key);

    ItemStack setInt(ItemStack item, String key, int value);

    Optional<Integer> getInt(ItemStack item, String key);

    boolean hasKey(ItemStack item, String key);

    ItemStack removeKey(ItemStack item, String key);
}
