package me.zamin.anchor.internal.compat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import me.zamin.anchor.api.diagnostics.DiagnosticSeverity;
import me.zamin.anchor.api.diagnostics.DoctorMessage;
import me.zamin.anchor.api.diagnostics.PluginCompatibilityReport;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public final class CompatibilityScanner {

    private static final List<String> DIRECT_SCHEDULER_MARKERS = List.of(
        "BukkitScheduler",
        "runTask",
        "runTaskLater",
        "runTaskTimer",
        "runTaskAsynchronously",
        "getScheduler"
    );

    private static final Map<String, CompatibilityProfile> KNOWN_PROFILES = Map.of(
        "plugman", new CompatibilityProfile(
            "PlugMan",
            DiagnosticSeverity.WARNING,
            "PLUGIN_RELOAD_TOOL",
            "PlugMan-style reload tools can destabilize Anchor service bindings.",
            "Runtime plugin reload tools are known to leave stale services, listeners, and scheduler state behind.",
            "Use a full server restart for production validation instead of hot-reload tools."
        ),
        "plugmanx", new CompatibilityProfile(
            "PlugManX",
            DiagnosticSeverity.WARNING,
            "PLUGIN_RELOAD_TOOL",
            "PlugMan-style reload tools can destabilize Anchor service bindings.",
            "Runtime plugin reload tools are known to leave stale services, listeners, and scheduler state behind.",
            "Use a full server restart for production validation instead of hot-reload tools."
        )
    );

    private final Plugin owner;

    public CompatibilityScanner(Plugin owner) {
        this.owner = owner;
    }

    public List<PluginCompatibilityReport> scanPlugins(boolean foliaDetected) {
        PluginManager pluginManager = owner.getServer().getPluginManager();
        List<PluginCompatibilityReport> reports = new ArrayList<>();
        for (Plugin candidate : pluginManager.getPlugins()) {
            if (candidate.getName().equalsIgnoreCase(owner.getName())) {
                continue;
            }

            List<DoctorMessage> issues = new ArrayList<>();
            boolean foliaDeclared = false;
            boolean directSchedulerUsage = false;
            try {
                Path jarPath = pluginJar(candidate);
                if (jarPath != null && Files.exists(jarPath)) {
                    foliaDeclared = containsPluginYamlFlag(jarPath, "folia-supported: true");
                    directSchedulerUsage = containsSchedulerMarkers(jarPath);
                }
            } catch (IOException ex) {
                issues.add(new DoctorMessage(
                    DiagnosticSeverity.WARNING,
                    "SCAN_FAILED",
                    "Plugin compatibility scan failed for " + candidate.getName() + ".",
                    ex.getMessage(),
                    "Rebuild the plugin jar or inspect the plugin manually if compatibility information is important."
                ));
            }

            if (foliaDetected && !foliaDeclared) {
                issues.add(new DoctorMessage(
                    DiagnosticSeverity.WARNING,
                    "MISSING_FOLIA_DECLARATION",
                    candidate.getName() + " is not marked folia-supported.",
                    "plugin.yml does not declare folia-supported: true.",
                    "Contact the plugin author or test carefully before production use on Folia."
                ));
            }
            if (foliaDetected && directSchedulerUsage) {
                issues.add(new DoctorMessage(
                    DiagnosticSeverity.WARNING,
                    "DIRECT_SCHEDULER_USAGE",
                    candidate.getName() + " may call BukkitScheduler directly.",
                    "Anchor found common direct scheduler markers in the plugin jar.",
                    "Review the plugin for Folia safety or prefer Anchor scheduler abstractions in your own code."
                ));
            }

            CompatibilityProfile profile = KNOWN_PROFILES.get(candidate.getName().toLowerCase(Locale.ROOT));
            if (profile != null) {
                issues.add(new DoctorMessage(
                    profile.severity(),
                    profile.code(),
                    profile.problem(),
                    profile.cause(),
                    profile.recommendedFix()
                ));
            }

            reports.add(new PluginCompatibilityReport(
                candidate.getName(),
                candidate.getPluginMeta().getVersion(),
                foliaDeclared,
                directSchedulerUsage,
                List.copyOf(issues)
            ));
        }
        return reports;
    }

    private Path pluginJar(Plugin candidate) {
        CodeSource source = candidate.getClass().getProtectionDomain().getCodeSource();
        if (source == null || source.getLocation() == null) {
            return null;
        }
        try {
            return Path.of(source.getLocation().toURI());
        } catch (URISyntaxException ex) {
            return Path.of(source.getLocation().getPath());
        }
    }

    private boolean containsPluginYamlFlag(Path jarPath, String flag) throws IOException {
        try (ZipFile zipFile = new ZipFile(jarPath.toFile())) {
            ZipEntry entry = zipFile.getEntry("plugin.yml");
            if (entry == null) {
                return false;
            }
            try (InputStream inputStream = zipFile.getInputStream(entry)) {
                String yaml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                return yaml.contains(flag);
            }
        }
    }

    private boolean containsSchedulerMarkers(Path jarPath) throws IOException {
        try (ZipFile zipFile = new ZipFile(jarPath.toFile())) {
            return zipFile.stream()
                .filter(entry -> entry.getName().endsWith(".class"))
                .anyMatch(entry -> classContainsMarker(zipFile, entry));
        }
    }

    private boolean classContainsMarker(ZipFile zipFile, ZipEntry entry) {
        try (InputStream inputStream = zipFile.getInputStream(entry)) {
            String contents = new String(inputStream.readAllBytes(), StandardCharsets.ISO_8859_1);
            return DIRECT_SCHEDULER_MARKERS.stream().anyMatch(contents::contains);
        } catch (IOException ex) {
            return false;
        }
    }
}
