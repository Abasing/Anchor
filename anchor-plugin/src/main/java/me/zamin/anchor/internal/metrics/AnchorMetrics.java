package me.zamin.anchor.internal.metrics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public final class AnchorMetrics {

    private final ConcurrentHashMap<String, LongAdder> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TimingBucket> timings = new ConcurrentHashMap<>();

    public void increment(String key) {
        add(key, 1L);
    }

    public void add(String key, long delta) {
        counters.computeIfAbsent(key, ignored -> new LongAdder()).add(delta);
    }

    public void recordTiming(String key, long nanos) {
        timings.computeIfAbsent(key, ignored -> new TimingBucket()).record(Math.max(0L, nanos));
    }

    public MetricsSnapshot snapshot() {
        Map<String, Long> counterCopy = new LinkedHashMap<>();
        counters.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> counterCopy.put(entry.getKey(), entry.getValue().sum()));

        Map<String, MetricSnapshot> timingCopy = new LinkedHashMap<>();
        timings.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> timingCopy.put(entry.getKey(), entry.getValue().snapshot()));
        return new MetricsSnapshot(counterCopy, timingCopy);
    }

    private static final class TimingBucket {

        private final LongAdder count = new LongAdder();
        private final LongAdder totalNanos = new LongAdder();
        private final AtomicLong maxNanos = new AtomicLong();

        private void record(long nanos) {
            count.increment();
            totalNanos.add(nanos);
            maxNanos.accumulateAndGet(nanos, Math::max);
        }

        private MetricSnapshot snapshot() {
            return new MetricSnapshot(count.sum(), totalNanos.sum(), maxNanos.get());
        }
    }
}
