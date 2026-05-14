package me.zamin.anchor.api.diagnostics;

import java.util.List;
import me.zamin.anchor.api.hooks.HookStatus;
import me.zamin.anchor.api.scheduler.SchedulerDiagnostics;

/**
 * Full diagnostics snapshot used by {@code /anchor doctor}.
 *
 * @param scheduler scheduler environment report
 * @param startup startup timing summary
 * @param hooks hook health and timing information
 * @param pluginReports plugin compatibility scan results
 * @param messages actionable warnings and informational notes
 */
public record DoctorReport(
    SchedulerDiagnostics scheduler,
    StartupTimingReport startup,
    List<HookStatus> hooks,
    List<PluginCompatibilityReport> pluginReports,
    List<DoctorMessage> messages
) {
}
