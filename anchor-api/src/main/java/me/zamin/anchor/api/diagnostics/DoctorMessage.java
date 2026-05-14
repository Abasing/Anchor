package me.zamin.anchor.api.diagnostics;

/**
 * Actionable diagnostics entry returned by {@link me.zamin.anchor.api.diagnostics.DiagnosticsService}.
 *
 * @param severity message severity
 * @param code stable machine-readable code
 * @param problem concise description of the issue
 * @param cause likely cause observed by Anchor
 * @param recommendedFix recommended next action
 */
public record DoctorMessage(
    DiagnosticSeverity severity,
    String code,
    String problem,
    String cause,
    String recommendedFix
) {
}
