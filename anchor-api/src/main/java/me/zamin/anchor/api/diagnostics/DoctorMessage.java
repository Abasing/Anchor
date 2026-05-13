package me.zamin.anchor.api.diagnostics;

public record DoctorMessage(
    DiagnosticSeverity severity,
    String code,
    String message
) {
}
