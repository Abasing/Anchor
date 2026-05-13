package me.zamin.anchor.api.diagnostics;

public record StartupTimingReport(
    long totalMillis,
    long schedulerMillis,
    long hooksMillis
) {
}
