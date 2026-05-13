# Anchor

Stop hooking into everything. Hook into Anchor.

Anchor is a Paper/Spigot library plugin that gives plugin developers one stable API for common ecosystem integrations such as economy, permissions, placeholders, regions, item metadata, GUI handling, and scheduling. Instead of directly wiring Vault, LuckPerms, PlaceholderAPI, WorldGuard, and other plugins into every project, developers depend on Anchor and let Anchor detect and bridge providers internally.

## Why Anchor exists

- Plugin authors should not have to maintain a matrix of plugin hooks in every project.
- Public APIs should stay stable even when internal adapters evolve.
- Fallback behavior should be safe when optional dependencies are missing.
- Server owners should be able to drop in one bridge plugin instead of solving dependency hell for every plugin.

## For plugin developers

Use Anchor as a runtime dependency and call the public API entrypoint:

```java
import dev.anchor.Anchor;
import dev.anchor.gui.AnchorGui;
import org.bukkit.inventory.ItemStack;

Anchor.api().economy().deposit(player.getUniqueId(), 100.0);

if (Anchor.api().permissions().has(player, "myplugin.admin")) {
    player.sendMessage("Admin access granted.");
}

String text = Anchor.api().placeholders().parse(player, "Hello {player}");
boolean canBuild = Anchor.api().regions().canBuild(player, location);

ItemStack tagged = Anchor.api().items().setString(item, "shop-id", "weapons");

AnchorGui.builder()
    .title("Anchor Example")
    .rows(3)
    .item(13, item, event -> event.getPlayer().sendMessage("Clicked!"))
    .open(player);
```

## For server owners

1. Build or download the Anchor jar.
2. Place it in the server `plugins/` folder.
3. Optionally install supported hook plugins such as Vault, LuckPerms, PlaceholderAPI, or WorldGuard.
4. Start the server and run `/anchor status`.

Anchor stays operational when optional hooks are absent. Missing integrations degrade to safe fallback providers instead of crashing the plugin.

## Current supported hooks

- Economy: Vault-backed economy when available, otherwise no-op fallback
- Permissions: LuckPerms, Vault permissions, then Bukkit fallback
- Placeholders: PlaceholderAPI bridge or internal placeholder provider
- Regions: WorldGuard bridge or configurable permissive fallback
- Items: PersistentDataContainer-based metadata
- Scheduler: Bukkit scheduler abstraction

## Build

```bash
mvn clean package
```

The built jar will be located at `target/anchor-1.0.0-SNAPSHOT.jar`.

## Documentation

- [Getting Started](docs/GETTING_STARTED.md)
- [API Examples](docs/API_EXAMPLES.md)
- [Adapters](docs/ADAPTERS.md)
- [Roadmap](docs/ROADMAP.md)
