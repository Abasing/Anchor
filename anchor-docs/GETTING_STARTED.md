# Getting Started

Anchor has two audiences:

- plugin developers who compile against `anchor-api`
- server owners who install `anchor-plugin`

## For plugin developers

1. Add `anchor-api` as a provided dependency.
2. Add `Anchor` as a soft dependency in `plugin.yml`.
3. Check `Anchor.isAvailable()` before using the API early in startup.
4. Use `Anchor.api()` as the only public entrypoint.

Minimal setup:

```xml
<dependency>
    <groupId>me.zamin</groupId>
    <artifactId>anchor-api</artifactId>
    <version>1.2.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

```yaml
softdepend:
  - Anchor
```

```java
if (Anchor.isAvailable()) {
    String provider = Anchor.api().permissions().providerName();
    getLogger().info("Permissions via " + provider);
}
```

## For server owners

1. Build or download `anchor-plugin`.
2. Put the jar in `plugins/`.
3. Install optional ecosystem plugins like Vault, LuckPerms, PlaceholderAPI, and WorldGuard as needed.
4. Start the server and run `/anchor doctor`.

## What to use first

Most consumer plugins start with these:

- `Anchor.api().scheduler()`
- `Anchor.api().permissions()`
- `Anchor.api().economy()`
- `Anchor.api().placeholders()`

That is usually enough to remove the worst direct hook sprawl in the first migration pass.
