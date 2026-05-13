package me.zamin.anchor.api.scheduler;

import java.util.List;

/**
 * Describes the runtime scheduler environment and Anchor's current safety posture.
 *
 * @param platform selected scheduler platform
 * @param foliaDetected whether Folia runtime classes were detected
 * @param globalSupported whether global scheduling is supported
 * @param regionSupported whether region-aware scheduling is supported
 * @param entitySupported whether entity-aware scheduling is supported
 * @param asyncSupported whether async scheduling is supported
 * @param implementationName implementation name for diagnostics output
 * @param warnings human-readable scheduler warnings
 */
public record SchedulerDiagnostics(
    SchedulerPlatform platform,
    boolean foliaDetected,
    boolean globalSupported,
    boolean regionSupported,
    boolean entitySupported,
    boolean asyncSupported,
    String implementationName,
    List<String> warnings
) {
}
