# Migration: LuckPerms

## Before

```java
LuckPerms luckPerms = Bukkit.getServicesManager().load(LuckPerms.class);
if (luckPerms != null) {
    User user = luckPerms.getUserManager().getUser(uuid);
}
```

## After

```java
if (Anchor.api().permissions().has(player, "myplugin.admin")) {
    // access granted
}
```

Anchor prefers LuckPerms, falls back to Vault permissions, then Bukkit permissions.
