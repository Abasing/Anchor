package me.zamin.anchor.testplugin;

import me.zamin.anchor.api.Anchor;
import me.zamin.anchor.api.gui.GuiFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class AnchorTestPlugin extends JavaPlugin {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        getLogger().info("Economy provider: " + Anchor.api().economy().providerName());
        getLogger().info("Permissions provider: " + Anchor.api().permissions().providerName());

        String parsed = Anchor.api().placeholders().parse(player, "Hello {player}, online: {online}");
        player.sendMessage(parsed);

        ItemStack emerald = Anchor.api().items().setString(new ItemStack(Material.EMERALD), "demo-key", "anchor");
        Anchor.api().guis().builder()
            .title("Anchor Example")
            .rows(3)
            .item(13, emerald, event -> event.getWhoClicked().sendMessage("Clicked through Anchor."))
            .open(player);

        Anchor.api().scheduler().runLaterGlobal(() -> player.sendMessage("Scheduled through Anchor."), 20L);
        return true;
    }
}
