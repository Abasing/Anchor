# Anchor

Stop hooking into everything. Hook into Anchor.

Anchor is a runtime plugin and public API for Paper and Spigot developers who are tired of wiring the same ecosystem hooks over and over.

Instead of every plugin talking to Vault, LuckPerms, PlaceholderAPI, WorldGuard, scheduler quirks, and item tag APIs directly, Anchor gives you one stable dependency and one runtime integration point.

## What Anchor is for

Anchor is focused on ecosystem abstraction, not random utility bloat.

Use it when your plugin needs to work with:

- economy providers through Vault
- permissions through LuckPerms, Vault, or Bukkit fallback
- PlaceholderAPI with internal fallback placeholders
- WorldGuard region checks with safe fallback behavior
- Paper and Folia-aware task scheduling
- item tags through PersistentDataContainer
- safe inventory GUI behavior

If your plugin already has direct hooks everywhere, Anchor is the migration target that removes that dependency sprawl.

## Why plugin developers use it

- One public API instead of five different plugin APIs.
- Missing optional plugins do not hard-crash your plugin.
- Paper and Folia scheduler differences stop leaking into every call site.
- Permission, economy, placeholder, and region hooks become predictable.
- Runtime diagnostics are built in with `/anchor doctor`.
- Consumer plugins can keep their own internal facades and adapt Anchor underneath them.

## Why server owners install it

- Plugins built against Anchor can support a wider ecosystem with less hook breakage.
- Missing integrations degrade cleanly instead of failing at startup.
- `/anchor status`, `/anchor hooks`, `/anchor doctor`, and `/anchor metrics` make integration state obvious.
- Anchor can sit in the background as shared infrastructure for multiple Zamin ecosystem plugins.

## Quick start for plugin developers

Add `anchor-api` as a provided dependency. Do not shade `anchor-plugin` into your plugin.

```xml
<dependency>
    <groupId>me.zamin</groupId>
    <artifactId>anchor-api</artifactId>
    <version>1.2.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

Add Anchor as a soft dependency in `plugin.yml`:

```yaml
softdepend:
  - Anchor
```

Then call the API through the single entrypoint:

```java
import me.zamin.anchor.api.Anchor;

Anchor.api().economy();
Anchor.api().permissions();
Anchor.api().placeholders();
Anchor.api().regions();
Anchor.api().items();
Anchor.api().guis();
Anchor.api().scheduler();
```

## Before Anchor vs After Anchor

### Before Anchor

```java
RegisteredServiceProvider<Economy> economy = Bukkit.getServicesManager().getRegistration(Economy.class);
if (economy != null) {
    economy.getProvider().depositPlayer(Bukkit.getOfflinePlayer(uuid), 100.0);
}

LuckPerms luckPerms = Bukkit.getServicesManager().load(LuckPerms.class);
if (luckPerms != null) {
    User user = luckPerms.getUserManager().getUser(uuid);
}

String parsed = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")
    ? PlaceholderAPI.setPlaceholders(player, text)
    : text;
```

### After Anchor

```java
import me.zamin.anchor.api.Anchor;

Anchor.api().economy().deposit(player.getUniqueId(), 100.0);

if (Anchor.api().permissions().has(player, "myplugin.admin")) {
    player.sendMessage("Admin access granted.");
}

Anchor.api().permissions().grantAsync(player.getUniqueId(), "myplugin.rank.vip")
    .thenAccept(result -> {
        if (!result.success()) {
            getLogger().warning(result.reason());
        }
    });

String text = Anchor.api().placeholders().parse(player, "Hello {player}");
boolean canBuild = Anchor.api().regions().canBuild(player, location);
Anchor.api().scheduler().entity(player).runLater(() -> player.sendMessage("Safe task"), 20L);
```

## API examples

Economy:

```java
Anchor.api().economy().deposit(player.getUniqueId(), 100.0);
double balance = Anchor.api().economy().getBalance(player.getUniqueId());
```

Permissions:

```java
if (Anchor.api().permissions().has(player, "myplugin.admin")) {
    player.sendMessage("Admin access granted.");
}
```

World-aware permissions:

```java
boolean allowed = Anchor.api().permissions().has(player.getUniqueId(), "world_nether", "myplugin.use");
```

Async permission mutation:

```java
Anchor.api().permissions().grantAsync(player.getUniqueId(), "myplugin.rank.vip")
    .thenAccept(result -> {
        if (!result.success()) {
            getLogger().warning(result.providerName() + ": " + result.reason());
        }
    });
```

Placeholders:

```java
String parsed = Anchor.api().placeholders().parse(player, "Hello {player}");
```

Regions:

```java
boolean canBuild = Anchor.api().regions().canBuild(player, location);
```

Item tags:

```java
ItemStack tagged = Anchor.api().items().setString(item, "shop-id", "weapons");
```

GUI:

```java
Anchor.api().guis().builder()
    .title("Anchor Example")
    .rows(3)
    .item(13, item, event -> event.getWhoClicked().sendMessage("Clicked"))
    .open(player);
```

Scheduler:

Anchor exposes platform-neutral scheduler contexts:

```java
Anchor.api().scheduler().global().run(...);
Anchor.api().scheduler().async().supplyAsync(...);
Anchor.api().scheduler().region(location).run(...);
Anchor.api().scheduler().entity(entity).run(...);
```

On Bukkit/Paper, region and entity contexts degrade cleanly to global scheduling. On Folia, Anchor uses the proper global, async, region, and entity schedulers internally.

## Supported hooks today

Real integrations:

- Vault economy
- LuckPerms permissions
- Vault permissions
- PlaceholderAPI
- WorldGuard
- Bukkit/Paper scheduler
- Folia-aware scheduler runtime
- PersistentDataContainer item tags

Fallback behavior:

- no-op economy when Vault economy is unavailable
- Bukkit permission checks when no stronger permissions provider is present
- internal placeholders when PlaceholderAPI is missing
- configurable permissive or deny region fallback when WorldGuard is missing

Reserved skeletons:

- Citizens
- ProtocolLib

## Commands

- `/anchor status`
- `/anchor hooks`
- `/anchor doctor`
- `/anchor metrics`
- `/anchor reload`

## Folia reality

Anchor does not pretend Folia is solved by adding `folia-supported: true`. PaperMC explicitly warns that the marker alone is not enough and that Folia requires using the correct global, region, async, and entity schedulers with no single main thread assumption.

Source: [PaperMC Docs: Supporting Paper and Folia](https://docs.papermc.io/paper/dev/folia-support/)

## Build

```powershell
$env:JAVA_HOME='F:\Zamin\InstalledSoftNOTOUCH'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn clean package
```

## Should I depend on `anchor-api` or `anchor-plugin`?

- Plugin developers should compile against `anchor-api`.
- Server owners should install `anchor-plugin`.
- `anchor-plugin` shades the API and adapter runtime into the installed jar.
- Other plugins should not shade `anchor-plugin`; they should depend on Anchor at runtime and call `Anchor.api()`.

## Modules

- `anchor-api`: stable public API for plugin developers
- `anchor-plugin`: installed runtime plugin
- `anchor-adapters`: Vault, LuckPerms, PlaceholderAPI, WorldGuard, Citizens skeleton, ProtocolLib skeleton
- `anchor-test-plugin`: copyable integration example
- `anchor-docs`: migration notes and design docs
- `examples/*`: independently compiling example plugins for focused usage patterns

## Docs

Start here:

- [Wiki](https://github.com/Abasing/Anchor/wiki)
- [Getting Started](/F:/Zamin/Plugins/Anchor/anchor-docs/GETTING_STARTED.md)
- [Dependency Setup](/F:/Zamin/Plugins/Anchor/anchor-docs/DEPENDENCY_SETUP.md)
- [API Usage](/F:/Zamin/Plugins/Anchor/anchor-docs/API_USAGE.md)
- [Commands](/F:/Zamin/Plugins/Anchor/anchor-docs/COMMANDS.md)
- [Docs Index](/F:/Zamin/Plugins/Anchor/anchor-docs/README.md)

Migration and runtime docs:

- `/anchor doctor`: runtime conditions, missing hooks, compatibility warnings, and validation findings
- `/anchor metrics`: lightweight operational timings and scheduler counters
- `examples/folia-safe-tasks`: focused scheduler usage example
- `examples/stress-test-plugin`: load-oriented scheduler validation example
- [Case Study: ZaminShop](/F:/Zamin/Plugins/Anchor/anchor-docs/CASE_STUDY_ZAMINSHOP.md)
- [Threading Model](/F:/Zamin/Plugins/Anchor/anchor-docs/THREADING_MODEL.md)
- [Runtime Testing](/F:/Zamin/Plugins/Anchor/anchor-docs/RUNTIME_TESTING.md)
