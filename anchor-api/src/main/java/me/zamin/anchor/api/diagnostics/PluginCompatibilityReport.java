package me.zamin.anchor.api.diagnostics;

import java.util.List;

/**
 * Compatibility scan result for a detected plugin.
 *
 * @param pluginName plugin name
 * @param pluginVersion plugin version
 * @param foliaDeclared whether the plugin declares Folia support in plugin.yml
 * @param possibleDirectSchedulerUsage whether Anchor detected likely direct Bukkit scheduler usage
 * @param issues actionable issues detected for this plugin
 */
public record PluginCompatibilityReport(
    String pluginName,
    String pluginVersion,
    boolean foliaDeclared,
    boolean possibleDirectSchedulerUsage,
    List<DoctorMessage> issues
) {
}
