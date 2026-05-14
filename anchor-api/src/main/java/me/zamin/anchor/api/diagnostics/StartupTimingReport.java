package me.zamin.anchor.api.diagnostics;

/**
 * Startup timing summary for the Anchor runtime.
 *
 * @param totalMillis total startup time for Anchor
 * @param schedulerMillis scheduler initialization time
 * @param hooksMillis hook discovery and provider selection time
 */
public record StartupTimingReport(
    long totalMillis,
    long schedulerMillis,
    long hooksMillis
) {
}
