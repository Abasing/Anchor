package me.zamin.anchor.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import me.zamin.anchor.api.Anchor;
import me.zamin.anchor.api.diagnostics.DoctorReport;
import me.zamin.anchor.api.diagnostics.PluginCompatibilityReport;
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
        sender.sendMessage(ChatColor.GRAY + " - Scheduler: " + Anchor.api().scheduler().providerName() + " [" + Anchor.api().scheduler().platform() + "]");
        sender.sendMessage(ChatColor.GRAY + " - Scheduler warnings: " + String.join("; ", Anchor.api().scheduler().diagnostics().warnings()));
        return true;
    }

    private boolean handleHooks(CommandSender sender) {
        if (!sender.hasPermission("anchor.command.hooks")) {
            return deny(sender);
        }
        sender.sendMessage(ChatColor.GOLD + "Anchor hooks");
        for (HookStatus hook : Anchor.api().hooks().all()) {
            sender.sendMessage(ChatColor.GRAY + " - " + hook.hookName() + " | " + hook.state() + " | " + hook.message() + " | " + hook.loadMillis() + "ms");
        }
        return true;
    }

    private boolean handleDoctor(CommandSender sender) {
        if (!sender.hasPermission("anchor.command.doctor")) {
            return deny(sender);
        }
        DoctorReport report = Anchor.api().diagnostics().doctor();
        sender.sendMessage(ChatColor.GOLD + "Anchor doctor");
        sender.sendMessage(ChatColor.YELLOW + "Scheduler: " + report.scheduler().platform() + " via " + report.scheduler().implementationName());
        sender.sendMessage(ChatColor.YELLOW + "Startup: total=" + report.startup().totalMillis() + "ms, scheduler=" + report.startup().schedulerMillis() + "ms, hooks=" + report.startup().hooksMillis() + "ms");
        for (HookStatus hook : report.hooks()) {
            sender.sendMessage(colorFor(hook.state()) + symbolFor(hook.state()) + " " + hook.hookName() + ChatColor.GRAY + " - " + hook.message());
        }
        for (PluginCompatibilityReport pluginReport : report.pluginReports()) {
            if (!pluginReport.issues().isEmpty()) {
                sender.sendMessage(ChatColor.YELLOW + "Plugin " + pluginReport.pluginName() + " " + pluginReport.pluginVersion());
                pluginReport.issues().forEach(issue -> sender.sendMessage(ChatColor.GRAY + " - [" + issue.severity() + "] " + issue.message()));
            }
        }
        for (var message : report.messages()) {
            sender.sendMessage((message.severity() == me.zamin.anchor.api.diagnostics.DiagnosticSeverity.ERROR ? ChatColor.RED
                : message.severity() == me.zamin.anchor.api.diagnostics.DiagnosticSeverity.WARNING ? ChatColor.YELLOW
                : ChatColor.GRAY) + "[" + message.code() + "] " + message.message());
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

    private String symbolFor(me.zamin.anchor.api.hooks.HookState state) {
        return switch (state) {
            case ACTIVE -> "✔";
            case FALLBACK, SKELETON -> "⚠";
            case MISSING, DISABLED, FAILED -> "✖";
        };
    }

    private ChatColor colorFor(me.zamin.anchor.api.hooks.HookState state) {
        return switch (state) {
            case ACTIVE -> ChatColor.GREEN;
            case FALLBACK, SKELETON -> ChatColor.YELLOW;
            case MISSING, DISABLED, FAILED -> ChatColor.RED;
        };
    }
}
