package me.zamin.anchor.examples.guidemo;

import me.zamin.anchor.api.Anchor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class GuiDemoPlugin extends JavaPlugin {

    public void openDemo(Player player) {
        ItemStack item = new ItemStack(Material.DIAMOND);
        Anchor.api().guis().builder()
            .title("Anchor GUI Demo")
            .rows(3)
            .item(13, item, event -> event.getWhoClicked().sendMessage("Clicked"))
            .open(player);
    }
}
