package me.zamin.anchor.api.diagnostics;

import me.zamin.anchor.api.AnchorService;
import me.zamin.anchor.api.scheduler.SchedulerDiagnostics;

public interface DiagnosticsService extends AnchorService {

    DoctorReport doctor();

    SchedulerDiagnostics schedulerDiagnostics();
}
