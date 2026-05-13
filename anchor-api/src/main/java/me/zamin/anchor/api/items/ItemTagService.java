package me.zamin.anchor.api.items;

import java.util.Optional;
import me.zamin.anchor.api.AnchorService;
import org.bukkit.inventory.ItemStack;

public interface ItemTagService extends AnchorService {

    ItemStack setString(ItemStack item, String key, String value);

    Optional<String> getString(ItemStack item, String key);

    ItemStack setInt(ItemStack item, String key, int value);

    Optional<Integer> getInt(ItemStack item, String key);

    boolean hasKey(ItemStack item, String key);

    ItemStack removeKey(ItemStack item, String key);
}
