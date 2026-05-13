package me.zamin.anchor.api.diagnostics;

import java.util.List;
import me.zamin.anchor.api.hooks.HookStatus;
import me.zamin.anchor.api.scheduler.SchedulerDiagnostics;

public record DoctorReport(
    SchedulerDiagnostics scheduler,
    StartupTimingReport startup,
    List<HookStatus> hooks,
    List<PluginCompatibilityReport> pluginReports,
    List<DoctorMessage> messages
) {
}
