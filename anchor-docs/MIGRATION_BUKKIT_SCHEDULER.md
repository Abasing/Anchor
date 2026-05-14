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

## Consumer plugin adapter pattern

If your plugin already has its own scheduler facade, do not rewrite every call
site just to adopt Anchor.

Write one adapter from your existing scheduler abstraction to Anchor's
`global()`, `async()`, `region(location)`, and `entity(entity)` contexts.

That keeps the migration local to your infrastructure code and lets the rest of
the plugin keep calling the same scheduler interface it already knows.

This was the cleanest migration path for ZaminShop: the plugin kept its own
`PlatformScheduler` contract, and only the implementation boundary changed when
Anchor was present.
