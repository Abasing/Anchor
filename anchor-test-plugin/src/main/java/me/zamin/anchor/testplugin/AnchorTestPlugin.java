package me.zamin.anchor.testplugin;

import me.zamin.anchor.api.Anchor;
import me.zamin.anchor.api.economy.EconomyService;
import me.zamin.anchor.api.permissions.PermissionsService;
import me.zamin.anchor.api.placeholders.PlaceholderService;
import me.zamin.anchor.api.regions.RegionService;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class AnchorTestPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!Anchor.isAvailable()) {
            getLogger().warning("Anchor API is not available yet.");
            return;
        }
        getLogger().info("Anchor detected.");
        logService("Economy", Anchor.api().economy().isAvailable(), Anchor.api().economy().providerName());
        logService("Permissions", Anchor.api().permissions().isAvailable(), Anchor.api().permissions().providerName());
        logService("Placeholders", Anchor.api().placeholders().isAvailable(), Anchor.api().placeholders().providerName());
        logService("Regions", Anchor.api().regions().isAvailable(), Anchor.api().regions().providerName());
        logService("Scheduler", true, Anchor.api().scheduler().providerName());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        EconomyService economy = Anchor.api().economy();
        PermissionsService permissions = Anchor.api().permissions();
        PlaceholderService placeholders = Anchor.api().placeholders();
        RegionService regions = Anchor.api().regions();

        player.sendMessage("Economy provider: " + economy.providerName() + " [" + economy.status() + "]");
        player.sendMessage("Permissions provider: " + permissions.providerName() + " [" + permissions.status() + "]");

        if (economy.isAvailable()) {
            player.sendMessage("Balance: " + economy.format(economy.getBalance(player.getUniqueId())));
        } else {
            player.sendMessage("Economy unavailable. Deposit and balance operations are intentionally skipped.");
        }

        boolean hasAdmin = permissions.has(player, "anchor.test.admin");
        player.sendMessage("Permission check anchor.test.admin = " + hasAdmin);

        String parsed = placeholders.parse(player, "Hello {player}, online: {online}, server: {server_version}");
        player.sendMessage(parsed);

        boolean canBuild = regions.canBuild(player, player.getLocation());
        player.sendMessage("Region check canBuild here = " + canBuild + " via " + regions.providerName());

        ItemStack emerald = Anchor.api().items().setString(new ItemStack(Material.EMERALD), "demo-key", "anchor");
        Anchor.api().guis().builder()
            .title("Anchor Example")
            .rows(3)
            .item(13, emerald, event -> event.getWhoClicked().sendMessage("Clicked through Anchor."))
            .open(player);

        Anchor.api().scheduler().global().run(() -> getLogger().info("Global scheduler demo executed."));
        Anchor.api().scheduler().async().runAsync(() -> getLogger().info("Async scheduler demo executed."));
        Anchor.api().scheduler().region(player.getLocation()).run(() -> player.sendMessage("Region scheduler demo executed."));
        Anchor.api().scheduler().entity(player).runLater(() -> player.sendMessage("Entity delayed scheduler demo executed."), 20L);
        return true;
    }

    private void logService(String name, boolean available, String provider) {
        getLogger().info(name + ": " + (available ? "available" : "fallback/unavailable") + " via " + provider);
    }
}
