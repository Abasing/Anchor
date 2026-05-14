package me.zamin.anchor.api.diagnostics;

import me.zamin.anchor.api.AnchorService;
import me.zamin.anchor.api.scheduler.SchedulerDiagnostics;

/**
 * Diagnostics and doctor-report service.
 */
public interface DiagnosticsService extends AnchorService {

    /**
     * Returns a full diagnostics report suitable for {@code /anchor doctor}.
     *
     * @return non-null doctor report
     */
    DoctorReport doctor();

    /**
     * Returns scheduler-only diagnostics.
     *
     * @return non-null scheduler diagnostics
     */
    SchedulerDiagnostics schedulerDiagnostics();
}
