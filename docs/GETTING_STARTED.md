# Getting Started

## Installing Anchor on a server

1. Build Anchor with `mvn clean package`.
2. Copy the generated jar into the server `plugins/` directory.
3. Install optional ecosystem plugins you want Anchor to bridge.
4. Start the server and verify status with `/anchor status`.

## Depending on Anchor from another plugin

Declare Anchor as a `softdepend` or `depend` in your own `plugin.yml`, then query the API through `Anchor.api()`.

```java
if (Anchor.isAvailable()) {
    double balance = Anchor.api().economy().getBalance(player.getUniqueId());
}
```

## Design contract

- `Anchor.api()` is the stable public entrypoint for Anchor 1.x.
- Services stay available even when an external hook is missing.
- Public interfaces are designed to remain source-compatible across 1.x releases.
