# Migration: Vault Economy

## Before

```java
Economy economy = Bukkit.getServicesManager().load(Economy.class);
if (economy != null) {
    economy.depositPlayer(Bukkit.getOfflinePlayer(uuid), 100.0);
}
```

## After

```java
Anchor.api().economy().deposit(uuid, 100.0);
```

Anchor handles missing Vault by exposing an unavailable service instead of crashing the dependent plugin.
