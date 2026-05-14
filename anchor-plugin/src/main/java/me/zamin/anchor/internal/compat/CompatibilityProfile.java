package me.zamin.anchor.internal.compat;

import me.zamin.anchor.api.diagnostics.DiagnosticSeverity;

record CompatibilityProfile(
    String pluginName,
    DiagnosticSeverity severity,
    String code,
    String problem,
    String cause,
    String recommendedFix
) {
}
