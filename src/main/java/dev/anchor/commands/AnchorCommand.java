package dev.anchor.commands;

import dev.anchor.adapters.Adapter;
import dev.anchor.core.AnchorService;
import dev.anchor.internal.core.AnchorBootstrap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public final class AnchorCommand implements CommandExecutor, TabCompleter {

    private final dev.anchor.AnchorPlugin plugin;

    public AnchorCommand(dev.anchor.AnchorPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String subcommand = args.length == 0 ? "status" : args[0].toLowerCase(Locale.ROOT);
        return switch (subcommand) {
            case "status" -> handleStatus(sender);
            case "hooks" -> handleHooks(sender);
            case "version" -> handleVersion(sender);
            case "reload" -> handleReload(sender);
            default -> {
                sender.sendMessage(ChatColor.RED + "Usage: /anchor <status|hooks|version|reload>");
                yield true;
            }
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("status", "hooks", "version", "reload");
        }
        return List.of();
    }

    private boolean handleStatus(CommandSender sender) {
        if (!sender.hasPermission("anchor.command.status")) {
            return noPermission(sender);
        }
        AnchorBootstrap bootstrap = plugin.bootstrap();
        sender.sendMessage(ChatColor.GOLD + "Anchor " + plugin.getPluginMeta().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Platform: " + bootstrap.platform().serverName() + " " + bootstrap.platform().minecraftVersion());
        sender.sendMessage(ChatColor.YELLOW + "Providers:");
        sendProviderLine(sender, "Economy", dev.anchor.Anchor.api().economy());
        sendProviderLine(sender, "Permissions", dev.anchor.Anchor.api().permissions());
        sendProviderLine(sender, "Placeholders", dev.anchor.Anchor.api().placeholders());
        sendProviderLine(sender, "Regions", dev.anchor.Anchor.api().regions());
        sendProviderLine(sender, "Items", dev.anchor.Anchor.api().items());
        sendProviderLine(sender, "Scheduler", dev.anchor.Anchor.api().scheduler());
        List<String> failed = bootstrap.adapterManager().adapters().stream()
            .filter(adapter -> adapter.getStatus() == dev.anchor.adapters.AdapterStatus.FAILED)
            .map(Adapter::getName)
            .toList();
        sender.sendMessage(ChatColor.YELLOW + "Failed adapters: " + (failed.isEmpty() ? "none" : String.join(", ", failed)));
        return true;
    }

    private boolean handleHooks(CommandSender sender) {
        if (!sender.hasPermission("anchor.command.hooks")) {
            return noPermission(sender);
        }
        sender.sendMessage(ChatColor.GOLD + "Anchor hooks");
        for (Adapter adapter : plugin.bootstrap().adapterManager().adapters()) {
            sender.sendMessage(ChatColor.GRAY + " - " + adapter.getName()
                + " | dependency=" + adapter.getPluginDependencyName()
                + " | priority=" + adapter.getPriority()
                + " | status=" + adapter.getStatus());
        }
        return true;
    }

    private boolean handleVersion(CommandSender sender) {
        if (!sender.hasPermission("anchor.command.version")) {
            return noPermission(sender);
        }
        sender.sendMessage(ChatColor.GOLD + "Anchor " + plugin.getPluginMeta().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Server: " + plugin.bootstrap().platform().serverVersion());
        sender.sendMessage(ChatColor.YELLOW + "Java: " + plugin.bootstrap().platform().javaVersion());
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("anchor.command.reload")) {
            return noPermission(sender);
        }
        plugin.reloadAnchor();
        sender.sendMessage(ChatColor.GREEN + "Anchor configuration reloaded.");
        return true;
    }

    private boolean noPermission(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
        return true;
    }

    private void sendProviderLine(CommandSender sender, String name, AnchorService service) {
        sender.sendMessage(ChatColor.GRAY + " - " + name + ": " + service.getProviderName() + " [" + service.getStatus() + "]");
    }
}
