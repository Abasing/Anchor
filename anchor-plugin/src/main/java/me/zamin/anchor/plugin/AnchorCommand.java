package me.zamin.anchor.plugin;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import me.zamin.anchor.api.Anchor;
import me.zamin.anchor.api.diagnostics.DiagnosticSeverity;
import me.zamin.anchor.api.diagnostics.DoctorMessage;
import me.zamin.anchor.api.diagnostics.DoctorReport;
import me.zamin.anchor.api.diagnostics.PluginCompatibilityReport;
import me.zamin.anchor.api.hooks.HookState;
import me.zamin.anchor.api.hooks.HookStatus;
import me.zamin.anchor.internal.metrics.MetricSnapshot;
import me.zamin.anchor.internal.metrics.MetricsSnapshot;
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
        long start = System.nanoTime();
        String subcommand = args.length == 0 ? "status" : args[0].toLowerCase(Locale.ROOT);
        boolean handled = switch (subcommand) {
            case "status" -> handleStatus(sender);
            case "hooks" -> handleHooks(sender);
            case "doctor" -> handleDoctor(sender);
            case "metrics" -> handleMetrics(sender);
            case "reload" -> handleReload(sender);
            default -> {
                sender.sendMessage(ChatColor.RED + "Usage: /anchor <status|hooks|doctor|metrics|reload>");
                yield true;
            }
        };
        plugin.runtime().recordCommandTiming(subcommand, System.nanoTime() - start);
        return handled;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return args.length == 1 ? List.of("status", "hooks", "doctor", "metrics", "reload") : List.of();
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

    private boolean handleMetrics(CommandSender sender) {
        if (!sender.hasPermission("anchor.command.metrics")) {
            return deny(sender);
        }
        MetricsSnapshot snapshot = plugin.runtime().metricsSnapshot();
        sender.sendMessage(ChatColor.GOLD + "Anchor metrics");
        sender.sendMessage(ChatColor.YELLOW + "Validation issues: " + plugin.runtime().validationReport().issues().size());
        for (Map.Entry<String, Long> entry : snapshot.counters().entrySet()) {
            sender.sendMessage(ChatColor.GRAY + " - " + entry.getKey() + ": " + entry.getValue());
        }
        for (Map.Entry<String, MetricSnapshot> entry : snapshot.timings().entrySet()) {
            MetricSnapshot metric = entry.getValue();
            sender.sendMessage(ChatColor.DARK_GRAY + " - " + entry.getKey()
                + ": count=" + metric.count()
                + ", avg=" + String.format(Locale.ROOT, "%.3f", metric.averageMillis()) + "ms"
                + ", max=" + String.format(Locale.ROOT, "%.3f", metric.maxMillis()) + "ms");
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
            sender.sendMessage(colorFor(hook.state()) + symbolFor(hook.state()) + " " + hook.hookName() + ChatColor.GRAY + " - " + hook.message() + " (" + hook.loadMillis() + "ms)");
        }
        for (PluginCompatibilityReport pluginReport : report.pluginReports()) {
            if (!pluginReport.issues().isEmpty()) {
                sender.sendMessage(ChatColor.YELLOW + "Plugin " + pluginReport.pluginName() + " " + pluginReport.pluginVersion());
                pluginReport.issues().forEach(issue -> sendDoctorMessage(sender, issue));
            }
        }
        for (DoctorMessage message : report.messages()) {
            sendDoctorMessage(sender, message);
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

    private String symbolFor(HookState state) {
        return switch (state) {
            case ACTIVE -> "[OK]";
            case FALLBACK, SKELETON -> "[WARN]";
            case MISSING, DISABLED, FAILED -> "[MISS]";
        };
    }

    private ChatColor colorFor(HookState state) {
        return switch (state) {
            case ACTIVE -> ChatColor.GREEN;
            case FALLBACK, SKELETON -> ChatColor.YELLOW;
            case MISSING, DISABLED, FAILED -> ChatColor.RED;
        };
    }

    private void sendDoctorMessage(CommandSender sender, DoctorMessage message) {
        ChatColor color = message.severity() == DiagnosticSeverity.ERROR
            ? ChatColor.RED
            : message.severity() == DiagnosticSeverity.WARNING ? ChatColor.YELLOW : ChatColor.GRAY;
        sender.sendMessage(color + message.severity().name() + ": " + message.problem());
        sender.sendMessage(ChatColor.DARK_GRAY + "Cause: " + ChatColor.GRAY + message.cause());
        sender.sendMessage(ChatColor.DARK_GRAY + "Fix: " + ChatColor.GRAY + message.recommendedFix());
    }
}
