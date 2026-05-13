# Migration: Direct Bukkit Scheduler Usage

## Before

```java
Bukkit.getScheduler().runTask(plugin, task);
Bukkit.getScheduler().runTaskLater(plugin, task, 20L);
Bukkit.getScheduler().runTaskTimer(plugin, task, 20L, 20L);
```

## After

```java
Anchor.api().scheduler().global().run(task);
Anchor.api().scheduler().global().runLater(task, 20L);
Anchor.api().scheduler().global().runRepeating(task, 20L, 20L);
```

## Folia-safe patterns

Location-owned work:

```java
Anchor.api().scheduler().region(location).run(() -> {
    location.getBlock().setType(Material.BEEHIVE);
});
```

Entity-owned work:

```java
Anchor.api().scheduler().entity(entity).run(() -> {
    entity.setGlowing(true);
});
```

Async work:

```java
Anchor.api().scheduler().async().supplyAsync(() -> expensiveLookup())
    .thenAccept(result -> Anchor.api().scheduler().entity(player).run(() -> applyResult(player, result)));
```
