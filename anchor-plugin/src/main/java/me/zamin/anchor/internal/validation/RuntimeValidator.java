package me.zamin.anchor.internal.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import me.zamin.anchor.api.AnchorService;
import me.zamin.anchor.api.diagnostics.PluginCompatibilityReport;
import me.zamin.anchor.api.economy.EconomyService;
import me.zamin.anchor.api.hooks.HookState;
import me.zamin.anchor.api.hooks.HookStatus;
import me.zamin.anchor.api.permissions.PermissionsService;
import me.zamin.anchor.api.placeholders.PlaceholderService;
import me.zamin.anchor.api.regions.RegionService;
import me.zamin.anchor.api.scheduler.SchedulerDiagnostics;
import me.zamin.anchor.api.scheduler.SchedulerService;
import me.zamin.anchor.internal.metrics.AnchorMetrics;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;

public final class RuntimeValidator {

    private final AnchorMetrics metrics;

    public RuntimeValidator(AnchorMetrics metrics) {
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    public ValidationReport validate(
        EconomyService economy,
        PermissionsService permissions,
        PlaceholderService placeholders,
        RegionService regions,
        SchedulerService scheduler,
        Collection<HookStatus> hooks,
        List<PluginCompatibilityReport> pluginReports
    ) {
        long start = System.nanoTime();
        List<ValidationIssue> issues = new ArrayList<>();

        validateScheduler(scheduler.diagnostics(), issues);
        validateProviders(economy, permissions, placeholders, regions, issues);
        validateAdapters(hooks, issues);
        validateCompatibility(pluginReports, issues);
        validateDuplicateProviders(issues);

        metrics.recordTiming("validation.runtime", System.nanoTime() - start);
        return new ValidationReport(System.currentTimeMillis(), List.copyOf(issues));
    }

    private void validateScheduler(SchedulerDiagnostics diagnostics, List<ValidationIssue> issues) {
        if (diagnostics.foliaDetected()) {
            issues.add(new ValidationIssue(
                ValidationSeverity.INFO,
                ValidationCategory.SCHEDULER,
                "Folia runtime detected.",
                "The server is using regionized threading instead of a single global main thread.",
                "Route work through Anchor scheduler contexts such as global(), region(location), entity(entity), and async()."
            ));
            if (!diagnostics.regionSupported() || !diagnostics.entitySupported()) {
                issues.add(new ValidationIssue(
                    ValidationSeverity.ERROR,
                    ValidationCategory.SCHEDULER,
                    "Folia was detected but the scheduler adapter is missing region or entity support.",
                    "Anchor did not expose the full Folia-aware scheduling surface at runtime.",
                    "Treat this server as unsupported until the scheduler adapter is corrected."
                ));
            }
        } else {
            issues.add(new ValidationIssue(
                ValidationSeverity.INFO,
                ValidationCategory.SCHEDULER,
                "Paper/Bukkit scheduler mode detected.",
                "Region and entity contexts degrade to the global scheduler on non-Folia platforms.",
                "Keep using Anchor scheduler contexts now so later Folia migration does not require API rewrites."
            ));
        }
    }

    private void validateProviders(EconomyService economy, PermissionsService permissions, PlaceholderService placeholders,
                                   RegionService regions, List<ValidationIssue> issues) {
        addUnavailableIssue("Economy", economy, issues);
        addUnavailableIssue("Permissions", permissions, issues);
        if (!placeholders.isAvailable()) {
            issues.add(new ValidationIssue(
                ValidationSeverity.INFO,
                ValidationCategory.PROVIDER,
                "External placeholder provider is unavailable.",
                "Anchor is using its internal placeholder resolver instead of PlaceholderAPI.",
                "Install PlaceholderAPI only if you need third-party placeholder expansion support."
            ));
        }
        if (!regions.isAvailable()) {
            issues.add(new ValidationIssue(
                ValidationSeverity.WARNING,
                ValidationCategory.PROVIDER,
                "Region provider is unavailable.",
                "Anchor is using the configured permissive or deny fallback instead of WorldGuard.",
                "Install WorldGuard or confirm that the configured fallback is acceptable for this server."
            ));
        }
    }

    private void validateAdapters(Collection<HookStatus> hooks, List<ValidationIssue> issues) {
        long activePermissionsHooks = hooks.stream()
            .filter(hook -> hook.state() == HookState.ACTIVE || hook.state() == HookState.FALLBACK)
            .filter(hook -> hook.hookName().toLowerCase().contains("permission") || hook.hookName().equalsIgnoreCase("LuckPerms"))
            .count();
        if (activePermissionsHooks > 1L) {
            issues.add(new ValidationIssue(
                ValidationSeverity.WARNING,
                ValidationCategory.ADAPTER,
                "Multiple permission hooks are active at the same time.",
                "Anchor detected more than one active or fallback permissions bridge.",
                "Prefer a single primary permissions provider and verify that Anchor selected the expected one."
            ));
        }
    }

    private void validateCompatibility(List<PluginCompatibilityReport> pluginReports, List<ValidationIssue> issues) {
        for (PluginCompatibilityReport report : pluginReports) {
            if (report.possibleDirectSchedulerUsage()) {
                issues.add(new ValidationIssue(
                    ValidationSeverity.WARNING,
                    ValidationCategory.THREADING,
                    report.pluginName() + " may use BukkitScheduler directly.",
                    "Anchor found direct scheduler markers during plugin jar scanning.",
                    "Review the plugin for Folia safety before production use."
                ));
            }
            report.issues().stream()
                .filter(issue -> issue.severity().name().equals("WARNING") || issue.severity().name().equals("ERROR"))
                .findAny()
                .ifPresent(issue -> issues.add(new ValidationIssue(
                    issue.severity().name().equals("ERROR") ? ValidationSeverity.ERROR : ValidationSeverity.WARNING,
                    ValidationCategory.COMPATIBILITY,
                    "Compatibility concerns detected for " + report.pluginName() + ".",
                    issue.problem(),
                    issue.recommendedFix()
                )));
        }
    }

    private void validateDuplicateProviders(List<ValidationIssue> issues) {
        int economyRegistrations = Bukkit.getServicesManager().getRegistrations(Economy.class).size();
        if (economyRegistrations > 1) {
            issues.add(new ValidationIssue(
                ValidationSeverity.WARNING,
                ValidationCategory.ADAPTER,
                "Multiple Vault economy providers are registered.",
                "Bukkit ServicesManager reported " + economyRegistrations + " economy registrations.",
                "Confirm the expected economy plugin is highest priority and test balance operations carefully."
            ));
        }

        int permissionRegistrations = Bukkit.getServicesManager().getRegistrations(Permission.class).size();
        if (permissionRegistrations > 1) {
            issues.add(new ValidationIssue(
                ValidationSeverity.WARNING,
                ValidationCategory.ADAPTER,
                "Multiple Vault permission providers are registered.",
                "Bukkit ServicesManager reported " + permissionRegistrations + " permission registrations.",
                "Confirm the expected permissions bridge is primary and verify group resolution on startup."
            ));
        }
    }

    private void addUnavailableIssue(String label, AnchorService service, List<ValidationIssue> issues) {
        if (!service.isAvailable()) {
            issues.add(new ValidationIssue(
                ValidationSeverity.WARNING,
                ValidationCategory.PROVIDER,
                label + " service is unavailable.",
                "Anchor selected the " + service.providerName() + " fallback or no-op implementation.",
                "Install the matching dependency plugin or keep handling the unavailable state explicitly."
            ));
        }
    }
}
