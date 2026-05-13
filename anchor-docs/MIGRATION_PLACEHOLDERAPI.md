# Migration: PlaceholderAPI

## Before

```java
String parsed = PlaceholderAPI.setPlaceholders(player, text);
```

## After

```java
String parsed = Anchor.api().placeholders().parse(player, text);
```

If PlaceholderAPI is missing, Anchor falls back to internal placeholders such as `{player}` and `{online}`.
