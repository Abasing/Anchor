# Migration: WorldGuard

## Before

Direct WorldGuard queries usually force plugin authors to carry WorldEdit and WorldGuard API details everywhere.

## After

```java
boolean canBuild = Anchor.api().regions().canBuild(player, location);
```

If WorldGuard is missing, Anchor uses configurable fallback behavior through `regions.default-permissive`.
