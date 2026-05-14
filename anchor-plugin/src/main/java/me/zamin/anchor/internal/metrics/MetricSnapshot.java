package me.zamin.anchor.internal.metrics;

public record MetricSnapshot(
    long count,
    long totalNanos,
    long maxNanos
) {
    public double averageMillis() {
        return count == 0L ? 0.0D : (totalNanos / 1_000_000.0D) / count;
    }

    public double maxMillis() {
        return maxNanos / 1_000_000.0D;
    }

    public double totalMillis() {
        return totalNanos / 1_000_000.0D;
    }
}
