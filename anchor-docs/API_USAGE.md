# API Usage

Anchor is intentionally narrow at the public entrypoint:

```java
Anchor.api()
```

Everything public starts there.

## Economy

```java
double balance = Anchor.api().economy().getBalance(player.getUniqueId());
Anchor.api().economy().deposit(player.getUniqueId(), 100.0);
```

If economy is unavailable, `isAvailable()` reports it and the fallback provider stays predictable.

## Permissions

Basic check:

```java
boolean allowed = Anchor.api().permissions().has(player, "myplugin.admin");
```

World-aware check:

```java
boolean allowed = Anchor.api().permissions().has(player.getUniqueId(), "world", "myplugin.use");
```

Async mutation:

```java
Anchor.api().permissions().grantAsync(player.getUniqueId(), "myplugin.rank.vip")
    .thenAccept(result -> {
        if (!result.success()) {
            getLogger().warning(result.providerName() + ": " + result.reason());
        }
    });
```

Use async mutation for command handlers, migrations, and bulk operations. Sync mutation is still available, but it may block depending on provider behavior.

## Placeholders

```java
String text = Anchor.api().placeholders().parse(player, "Hello {player}");
```

Internal fallback placeholders include:

- `{player}`
- `{uuid}`
- `{world}`
- `{online}`
- `{server_version}`

## Regions

```java
if (Anchor.api().regions().canBuild(player, location)) {
    // safe to continue
}
```

## Item tags

```java
ItemStack tagged = Anchor.api().items().setString(item, "shop-id", "weapons");
```

## GUI

```java
Anchor.api().guis().builder()
    .title("Anchor Example")
    .rows(3)
    .item(13, item, event -> event.getWhoClicked().sendMessage("Clicked"))
    .open(player);
```

## Scheduler

Global:

```java
Anchor.api().scheduler().global().run(() -> doGlobalWork());
```

Async:

```java
Anchor.api().scheduler().async().supplyAsync(() -> loadData());
```

Region:

```java
Anchor.api().scheduler().region(location).run(() -> updateBlockState());
```

Entity:

```java
Anchor.api().scheduler().entity(player).runLater(() -> player.sendMessage("Done"), 20L);
```

If your plugin already has its own scheduler wrapper, adapt Anchor under that facade instead of rewriting every call site.
