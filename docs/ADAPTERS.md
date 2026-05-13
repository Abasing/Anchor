# Adapters

Anchor treats every external integration as an adapter. Adapters are loaded through the internal adapter manager, which handles detection, lifecycle, logging, and failure isolation.

## Adapter rules

- Adapters must never leak external plugin types into Anchor's public API.
- Adapter failures should be logged and isolated from the rest of the plugin.
- Provider selection uses explicit priorities so the best available bridge wins consistently.

## Initial adapters

- `VaultEconomyProvider`
- `VaultPermissionsProvider`
- `LuckPermsPermissionsProvider`
- `PlaceholderAPIProvider`
- `WorldGuardRegionProvider`

## Fallback providers

- `NoOpEconomyProvider`
- `BukkitPermissionsProvider`
- `InternalPlaceholderProvider`
- `NoOpRegionProvider`

Fallback providers keep service access stable even when no external hook is available.
