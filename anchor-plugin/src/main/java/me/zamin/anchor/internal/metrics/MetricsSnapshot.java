package me.zamin.anchor.internal.metrics;

import java.util.Map;
import java.util.Objects;

public record MetricsSnapshot(
    Map<String, Long> counters,
    Map<String, MetricSnapshot> timings
) {
    public MetricsSnapshot {
        Objects.requireNonNull(counters, "counters");
        Objects.requireNonNull(timings, "timings");
        counters = Map.copyOf(counters);
        timings = Map.copyOf(timings);
    }
}
