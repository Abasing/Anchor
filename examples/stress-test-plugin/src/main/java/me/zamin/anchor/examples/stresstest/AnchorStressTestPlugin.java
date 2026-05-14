package me.zamin.anchor.examples.stresstest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import me.zamin.anchor.api.Anchor;
import me.zamin.anchor.api.scheduler.SchedulerPlatform;
import me.zamin.anchor.api.scheduler.TaskHandle;
import me.zamin.anchor.api.scheduler.TaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class AnchorStressTestPlugin extends JavaPlugin implements CommandExecutor {

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ConcurrentMap<String, List<TaskHandle>> activeTasks = new ConcurrentHashMap<>();
    private final StressMetrics metrics = new StressMetrics();

    @Override
    public void onEnable() {
        PluginCommand command = getCommand("anchorstresstest");
        if (command == null) {
            throw new IllegalStateException("anchorstresstest command missing from plugin.yml");
        }
        command.setExecutor(this);
    }

    @Override
    public void onDisable() {
        stopStress();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("/anchorstresstest <start|stop|report>");
            return true;
        }
        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "start" -> startStress(sender);
            case "stop" -> stopStress(sender);
            case "report" -> report(sender);
            default -> {
                sender.sendMessage("/anchorstresstest <start|stop|report>");
                yield true;
            }
        };
    }

    private boolean startStress(CommandSender sender) {
        if (!running.compareAndSet(false, true)) {
            sender.sendMessage("Stress test already running.");
            return true;
        }

        metrics.reset(Anchor.api().scheduler().platform());
        activeTasks.clear();

        Location regionLocation = defaultLocation();
        List<Player> players = List.copyOf(Bukkit.getOnlinePlayers());

        scheduleBurst("global", Anchor.api().scheduler().global(), 75, 5L, 20L);
        scheduleBurst("async", Anchor.api().scheduler().async(), 150, 10L, 10L);
        if (regionLocation != null) {
            scheduleBurst("region", Anchor.api().scheduler().region(regionLocation), 100, 15L, 20L);
        }
        for (Player player : players) {
            scheduleBurst("entity-" + player.getUniqueId(), Anchor.api().scheduler().entity(player), 40, 20L, 20L);
        }

        Anchor.api().scheduler().global().runLater(this::cancelHalfOfRepeating, 120L);
        Anchor.api().scheduler().global().runLater(() -> {
            if (running.get()) {
                getLogger().info("Anchor stress test still running. Use /anchorstresstest stop or /anchorstresstest report.");
            }
        }, 200L);

        sender.sendMessage("Anchor stress test started on " + Anchor.api().scheduler().platform() + ".");
        return true;
    }

    private void scheduleBurst(String context, TaskScheduler scheduler, int count, long delayTicks, long repeatingPeriodTicks) {
        for (int index = 0; index < count; index++) {
            scheduleOneShot(context, scheduler, index);
            scheduleDelayed(context, scheduler, delayTicks + (index % 5));
        }
        for (int index = 0; index < Math.max(3, count / 25); index++) {
            scheduleRepeating(context, scheduler, delayTicks, repeatingPeriodTicks);
        }
    }

    private void scheduleOneShot(String context, TaskScheduler scheduler, int index) {
        long submittedAt = System.nanoTime();
        TaskHandle handle = scheduler.run(() -> {
            metrics.recordDelay(context, System.nanoTime() - submittedAt);
            long start = System.nanoTime();
            busyWork(index);
            metrics.recordRuntime(context, System.nanoTime() - start);
        });
        attachCompletion(context, submittedAt, handle);
        track(context, handle);
    }

    private void scheduleDelayed(String context, TaskScheduler scheduler, long delayTicks) {
        long submittedAt = System.nanoTime();
        TaskHandle handle = scheduler.runLater(() -> {
            metrics.recordDelay(context + "-delayed", System.nanoTime() - submittedAt);
            long start = System.nanoTime();
            busyWork(8);
            metrics.recordRuntime(context + "-delayed", System.nanoTime() - start);
        }, delayTicks);
        attachCompletion(context + "-delayed", submittedAt, handle);
        track(context, handle);
    }

    private void scheduleRepeating(String context, TaskScheduler scheduler, long delayTicks, long periodTicks) {
        long submittedAt = System.nanoTime();
        AtomicBoolean firstRun = new AtomicBoolean(true);
        TaskHandle handle = scheduler.runRepeating(() -> {
            if (firstRun.compareAndSet(true, false)) {
                metrics.recordDelay(context + "-repeating", System.nanoTime() - submittedAt);
            }
            long start = System.nanoTime();
            busyWork(5);
            metrics.recordRuntime(context + "-repeating", System.nanoTime() - start);
        }, delayTicks, periodTicks);
        attachCompletion(context + "-repeating", submittedAt, handle);
        track(context, handle);
    }

    private void attachCompletion(String context, long submittedAt, TaskHandle handle) {
        handle.onComplete((completed, throwable) -> {
            metrics.recordCallback(context, System.nanoTime() - submittedAt);
            if (throwable != null) {
                metrics.incrementFailure(context);
            }
            if (completed.isCancelled()) {
                metrics.incrementCancellation(context);
            }
        });
    }

    private void track(String context, TaskHandle handle) {
        activeTasks.computeIfAbsent(context, ignored -> new CopyOnWriteArrayList<>()).add(handle);
    }

    private void cancelHalfOfRepeating() {
        activeTasks.forEach((context, handles) -> {
            int cancelled = 0;
            for (TaskHandle handle : List.copyOf(handles)) {
                if (handle.isRepeating() && cancelled++ % 2 == 0) {
                    handle.cancel();
                }
            }
        });
    }

    private boolean stopStress(CommandSender sender) {
        stopStress();
        sender.sendMessage("Anchor stress test stopped.");
        return true;
    }

    private void stopStress() {
        running.set(false);
        activeTasks.values().forEach(handles -> handles.forEach(TaskHandle::cancel));
        activeTasks.clear();
    }

    private boolean report(CommandSender sender) {
        sender.sendMessage("Anchor stress report");
        sender.sendMessage("Platform: " + metrics.platform());
        metrics.render().forEach(sender::sendMessage);
        return true;
    }

    private Location defaultLocation() {
        Player online = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        if (online != null) {
            return online.getLocation();
        }
        World world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
        return world == null ? null : world.getSpawnLocation();
    }

    private void busyWork(int seed) {
        double accumulator = seed;
        for (int i = 0; i < 1_000; i++) {
            accumulator += Math.sqrt(i + accumulator);
        }
        if (Double.isNaN(accumulator)) {
            throw new IllegalStateException("Unexpected stress math state.");
        }
    }

    private static final class StressMetrics {

        private final ConcurrentMap<String, TimingBucket> delays = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, TimingBucket> runtimes = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, TimingBucket> callbacks = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, LongAdder> cancellations = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, LongAdder> failures = new ConcurrentHashMap<>();
        private volatile SchedulerPlatform platform = SchedulerPlatform.BUKKIT;

        void reset(SchedulerPlatform platform) {
            this.platform = Objects.requireNonNull(platform, "platform");
            delays.clear();
            runtimes.clear();
            callbacks.clear();
            cancellations.clear();
            failures.clear();
        }

        void recordDelay(String context, long nanos) {
            delays.computeIfAbsent(context, ignored -> new TimingBucket()).record(nanos);
        }

        void recordRuntime(String context, long nanos) {
            runtimes.computeIfAbsent(context, ignored -> new TimingBucket()).record(nanos);
        }

        void recordCallback(String context, long nanos) {
            callbacks.computeIfAbsent(context, ignored -> new TimingBucket()).record(nanos);
        }

        void incrementCancellation(String context) {
            cancellations.computeIfAbsent(context, ignored -> new LongAdder()).increment();
        }

        void incrementFailure(String context) {
            failures.computeIfAbsent(context, ignored -> new LongAdder()).increment();
        }

        SchedulerPlatform platform() {
            return platform;
        }

        List<String> render() {
            List<String> lines = new ArrayList<>();
            List<String> contexts = new ArrayList<>(delays.keySet());
            contexts.sort(Comparator.naturalOrder());
            for (String context : contexts) {
                TimingBucket delay = delays.getOrDefault(context, new TimingBucket());
                TimingBucket runtime = runtimes.getOrDefault(context, new TimingBucket());
                TimingBucket callback = callbacks.getOrDefault(context, new TimingBucket());
                long cancelled = cancellations.getOrDefault(context, new LongAdder()).sum();
                long failed = failures.getOrDefault(context, new LongAdder()).sum();
                lines.add(context
                    + " | tasks=" + Math.max(delay.count(), runtime.count())
                    + " | avgDelay=" + formatMillis(delay.averageNanos())
                    + " | avgRuntime=" + formatMillis(runtime.averageNanos())
                    + " | avgCallback=" + formatMillis(callback.averageNanos())
                    + " | cancelled=" + cancelled
                    + " | failures=" + failed);
            }
            if (lines.isEmpty()) {
                lines.add("No stress metrics collected yet.");
            }
            return lines;
        }

        private String formatMillis(double nanos) {
            return String.format(Locale.ROOT, "%.3fms", nanos / 1_000_000.0D);
        }
    }

    private static final class TimingBucket {

        private final LongAdder count = new LongAdder();
        private final LongAdder totalNanos = new LongAdder();

        void record(long nanos) {
            count.increment();
            totalNanos.add(Math.max(0L, nanos));
        }

        long count() {
            return count.sum();
        }

        double averageNanos() {
            return count.sum() == 0L ? 0.0D : (double) totalNanos.sum() / count.sum();
        }
    }
}
