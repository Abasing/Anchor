package me.zamin.anchor.examples.foliasafe;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.zamin.anchor.api.Anchor;
import me.zamin.anchor.api.scheduler.TaskHandle;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class FoliaSafeTasksPlugin extends JavaPlugin {

    private final Map<UUID, TaskHandle> repeatingTasks = new ConcurrentHashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        runGlobalTask();
        runAsyncTask();
        runRegionTask(player.getLocation());
        runEntityTask(player);
        runDelayedTask(player);
        runRepeatingTask(player);
        runCompletionFuture(player);
        return true;
    }

    // Use global() for work tied to server-global state rather than a specific entity or region.
    private void runGlobalTask() {
        Anchor.api().scheduler().global().run(() -> getLogger().info("Global task executed through Anchor."));
    }

    // Use async() for CPU or I/O work that must not touch Bukkit world state directly.
    private void runAsyncTask() {
        Anchor.api().scheduler().async().supplyAsync(() -> "async-value")
            .thenAccept(value -> getLogger().info("Async task completed with " + value));
    }

    // Use region(location) for block, chunk, or world state owned by a location.
    private void runRegionTask(Location location) {
        Anchor.api().scheduler().region(location).run(() -> location.getBlock().getState().update());
    }

    // Use entity(entity) for work owned by an entity so Folia can follow teleports safely.
    private void runEntityTask(Entity entity) {
        Anchor.api().scheduler().entity(entity).run(() -> entity.setGlowing(true));
    }

    // Delayed tasks stay in the same ownership context instead of assuming a single main thread.
    private void runDelayedTask(Player player) {
        Anchor.api().scheduler().entity(player).runLater(() -> player.sendMessage("Delayed task through Anchor."), 20L);
    }

    // Repeating tasks return cancellable handles that should be tracked explicitly.
    private void runRepeatingTask(Player player) {
        TaskHandle existing = repeatingTasks.remove(player.getUniqueId());
        if (existing != null) {
            existing.cancel();
        }
        TaskHandle repeating = Anchor.api().scheduler().entity(player).runRepeating(() -> player.sendMessage("Repeating tick through Anchor."), 20L, 40L);
        repeating.onComplete((handle, throwable) -> player.sendMessage("Repeating task completed or cancelled."));
        repeatingTasks.put(player.getUniqueId(), repeating);

        Anchor.api().scheduler().entity(player).runLater(() -> {
            TaskHandle handle = repeatingTasks.remove(player.getUniqueId());
            if (handle != null) {
                handle.cancel();
                player.sendMessage("Repeating task cancelled through Anchor.");
            }
        }, 120L);
    }

    // Completion futures and callbacks allow code to observe task completion without Folia-specific classes.
    private void runCompletionFuture(Player player) {
        TaskHandle handle = Anchor.api().scheduler().entity(player).run(() -> player.sendMessage("One-shot entity task."));
        handle.onComplete((completed, throwable) -> player.sendMessage("Callback invoked for task " + completed.taskId()));
        handle.completionFuture().thenRun(() -> getLogger().info("Completion future resolved for task " + handle.taskId()));
    }
}
