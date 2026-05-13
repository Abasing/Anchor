package me.zamin.anchor.api.diagnostics;

import java.util.List;

public record DoctorReport(
    List<String> installedHooks,
    List<String> missingHooks,
    List<String> unavailableServices,
    List<String> notes
) {
}
