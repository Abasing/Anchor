package me.zamin.anchor.api.diagnostics;

import java.util.List;

public record PluginCompatibilityReport(
    String pluginName,
    String pluginVersion,
    boolean foliaDeclared,
    boolean possibleDirectSchedulerUsage,
    List<DoctorMessage> issues
) {
}
