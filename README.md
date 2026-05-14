# Anchor

Stop hooking into everything. Hook into Anchor.

Anchor is a serious ecosystem integration layer for Paper and Spigot plugin developers. Instead of every plugin wiring Vault, LuckPerms, PlaceholderAPI, WorldGuard, scheduler quirks, GUI safety, and item tags on its own, Anchor provides one stable public API and hides the adapter churn behind it.

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

String text = Anchor.api().placeholders().parse(player, "Hello {player}");
boolean canBuild = Anchor.api().regions().canBuild(player, location);
Anchor.api().scheduler().entity(player).runLater(() -> player.sendMessage("Safe task"), 20L);
```

## What makes Anchor worth using

- Painful hooks first: Vault economy, LuckPerms permissions, PlaceholderAPI, WorldGuard, scheduler abstraction, PDC item tags, and GUI safety.
- Missing plugins do not crash downstream plugins.
- Direct plugin hooks start to look like technical debt.
- Example plugins are included as real buildable modules.
- Migration notes are included in `anchor-docs`.
- `/anchor doctor` is designed to explain runtime conditions instead of just dumping booleans.

## Scheduler Direction

Anchor exposes platform-neutral scheduler contexts:

```java
Anchor.api().scheduler().global().run(...);
Anchor.api().scheduler().async().supplyAsync(...);
Anchor.api().scheduler().region(location).run(...);
Anchor.api().scheduler().entity(entity).run(...);
```

On Bukkit/Paper, region and entity contexts degrade cleanly to global scheduling. On Folia, Anchor uses the proper global, async, region, and entity schedulers internally.

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
