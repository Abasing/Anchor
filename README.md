# Anchor

Stop hooking into everything. Hook into Anchor.

Anchor is a serious ecosystem integration layer for Paper and Spigot plugin developers. Instead of every plugin wiring Vault, LuckPerms, PlaceholderAPI, WorldGuard, scheduler quirks, GUI safety, and item tags on its own, Anchor provides one stable public API and hides the adapter churn behind it.

## Project Layout

```txt
anchor-parent
├── anchor-api
├── anchor-plugin
├── anchor-adapters
├── anchor-test-plugin
└── anchor-docs
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

String text = Anchor.api().placeholders().parse(player, "Hello {player}");
boolean canBuild = Anchor.api().regions().canBuild(player, location);
```

## What makes Anchor worth using

- Painful hooks first: Vault economy, LuckPerms permissions, PlaceholderAPI, WorldGuard, scheduler abstraction, PDC item tags, and GUI safety.
- Missing plugins do not crash downstream plugins.
- Direct plugin hooks start to look like technical debt.
- Example plugin included in `anchor-test-plugin`.
- Migration notes included in `anchor-docs`.

## Folia reality

Anchor does not pretend Folia is solved by adding `folia-supported: true`. PaperMC explicitly warns that the marker alone is not enough and that Folia requires using the correct global, region, async, and entity schedulers with no single main thread assumption. Anchor's scheduler API is designed to grow into that model, but the current runtime implementation is Bukkit/Paper-first with Folia-aware surface design.

Source: [PaperMC Docs: Supporting Paper and Folia](https://docs.papermc.io/paper/dev/folia-support/)

## Build

```powershell
$env:JAVA_HOME='F:\Zamin\InstalledSoftNOTOUCH'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn clean package
```

## Modules

- `anchor-api`: stable public API for plugin developers
- `anchor-plugin`: installed runtime plugin
- `anchor-adapters`: Vault, LuckPerms, PlaceholderAPI, WorldGuard, Citizens skeleton, ProtocolLib skeleton
- `anchor-test-plugin`: copyable integration example
- `anchor-docs`: migration notes and design docs
