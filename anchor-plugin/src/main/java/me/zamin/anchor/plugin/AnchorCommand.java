package me.zamin.anchor.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import me.zamin.anchor.api.Anchor;
import me.zamin.anchor.api.diagnostics.DoctorReport;
import me.zamin.anchor.api.hooks.HookStatus;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public final class AnchorCommand implements CommandExecutor, TabCompleter {

    private final AnchorPlugin plugin;

    public AnchorCommand(AnchorPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String subcommand = args.length == 0 ? "status" : args[0].toLowerCase(Locale.ROOT);
        switch (subcommand) {
            case "status":
                return handleStatus(sender);
            case "hooks":
                return handleHooks(sender);
            case "doctor":
                return handleDoctor(sender);
            case "reload":
                return handleReload(sender);
            default:
                sender.sendMessage(ChatColor.RED + "Usage: /anchor <status|hooks|doctor|reload>");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("status", "hooks", "doctor", "reload");
        }
        return List.of();
    }

    private boolean handleStatus(CommandSender sender) {
        if (!sender.hasPermission("anchor.command.status")) {
            return deny(sender);
        }
        sender.sendMessage(ChatColor.GOLD + "Anchor " + plugin.getPluginMeta().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Platform: " + Anchor.api().platform().serverName() + " " + Anchor.api().platform().minecraftVersion());
        sender.sendMessage(ChatColor.GRAY + " - Economy: " + Anchor.api().economy().providerName() + " [" + Anchor.api().economy().status() + "]");
        sender.sendMessage(ChatColor.GRAY + " - Permissions: " + Anchor.api().permissions().providerName() + " [" + Anchor.api().permissions().status() + "]");
        sender.sendMessage(ChatColor.GRAY + " - Placeholders: " + Anchor.api().placeholders().providerName() + " [" + Anchor.api().placeholders().status() + "]");
        sender.sendMessage(ChatColor.GRAY + " - Regions: " + Anchor.api().regions().providerName() + " [" + Anchor.api().regions().status() + "]");
        sender.sendMessage(ChatColor.GRAY + " - Items: " + Anchor.api().items().providerName() + " [" + Anchor.api().items().status() + "]");
        sender.sendMessage(ChatColor.GRAY + " - GUI: " + Anchor.api().guis().providerName() + " [" + Anchor.api().guis().status() + "]");
        sender.sendMessage(ChatColor.GRAY + " - Scheduler: " + Anchor.api().scheduler().providerName() + " [" + Anchor.api().scheduler().status() + "]");
        return true;
    }

    private boolean handleHooks(CommandSender sender) {
        if (!sender.hasPermission("anchor.command.hooks")) {
            return deny(sender);
        }
        sender.sendMessage(ChatColor.GOLD + "Anchor hooks");
        for (HookStatus hook : Anchor.api().hooks().all()) {
            sender.sendMessage(ChatColor.GRAY + " - " + hook.hookName() + " | " + hook.state() + " | " + hook.message());
        }
        return true;
    }

    private boolean handleDoctor(CommandSender sender) {
        if (!sender.hasPermission("anchor.command.doctor")) {
            return deny(sender);
        }
        DoctorReport report = Anchor.api().diagnostics().doctor();
        sender.sendMessage(ChatColor.GOLD + "Anchor doctor");
        sender.sendMessage(ChatColor.YELLOW + "Installed hooks: " + String.join(", ", report.installedHooks()));
        sender.sendMessage(ChatColor.YELLOW + "Missing hooks: " + String.join(", ", report.missingHooks()));
        sender.sendMessage(ChatColor.YELLOW + "Unavailable services: " + String.join(", ", report.unavailableServices()));
        for (String note : report.notes()) {
            sender.sendMessage(ChatColor.GRAY + " - " + note);
        }
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("anchor.command.reload")) {
            return deny(sender);
        }
        plugin.reloadAnchor();
        sender.sendMessage(ChatColor.GREEN + "Anchor reloaded.");
        return true;
    }

    private boolean deny(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
        return true;
    }
}
