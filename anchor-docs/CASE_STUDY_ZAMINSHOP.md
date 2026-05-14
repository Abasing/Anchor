# Case Study: ZaminShop

ZaminShop was the first real consumer migration used to validate whether Anchor
reduced complexity in a live plugin instead of just looking clean in examples.

## What was migrated

- scheduler integration through an adapter from ZaminShop's existing scheduler facade to Anchor
- common permission checks through Anchor permissions when available
- Vault-style economy resolution through Anchor economy when the configured shop economy used the Vault path
- startup logging for Anchor detection, scheduler platform, enabled features, and unavailable services

## What stayed internal

- non-Vault economy providers stayed internal because ZaminShop supports many plugin-specific currencies that Anchor does not abstract yet
- permission mutation for the actual shop purchase flow stayed direct during the first pass because Anchor originally lacked safe mutation support and world-aware permission checks
- menu and GUI systems stayed internal because replacing them with Anchor GUI would have been a rewrite, not a cleanup
- placeholder handling stayed internal because ZaminShop did not have a direct PlaceholderAPI parsing seam that needed replacing

## What Anchor improved

- Folia-aware scheduling became reusable without rewriting every scheduler call site
- common permission checks no longer needed to know whether the server preferred LuckPerms, Vault, or Bukkit fallback
- the Vault economy path no longer needed to wire directly into Vault when Anchor was present
- startup logs made the runtime integration state obvious

## API gaps discovered

- Anchor needed world-aware permission checks
- Anchor needed safe permission mutation results instead of boolean-or-throw behavior
- Anchor needed async-safe permission mutation so command-driven changes would not hide provider blocking behind a clean synchronous method
- Anchor needed batch permission mutation semantics for purchases that grant more than one permission and may need best-effort rollback
- consumer plugins with their own scheduler facade needed clear migration guidance

## Permission mutation: sync vs async

Anchor now supports both sync and async permission mutation.

- sync mutation is still useful for small convenience calls where the caller understands provider cost
- async mutation is the preferred path for commands, migrations, bulk permission grants, and any flow that may load or save provider state
- LuckPerms now uses native async user load and save paths
- non-native providers can still be used safely through Anchor's async scheduler bridge

## Batch permission mutation and best-effort rollback

ZaminShop exposed the next real gap after single-permission async mutation was in place:

- a purchase can grant several permissions
- one grant may fail after earlier grants already succeeded
- refunding money is not enough if permission state is left partially applied

Anchor now exposes batch mutation results that make partial success visible and
allow best-effort rollback for providers that can support it.

This is intentionally not described as a transaction API. Providers vary, and
callers still need to inspect the batch result instead of assuming all-or-nothing behavior.

## Why Anchor stayed optional

ZaminShop already had mature fallback paths for schedulers, non-Vault economies,
and permission handling.

Making Anchor optional let the plugin prove the integration value without
forcing every server to install Anchor immediately.

## Runtime testing checklist

- start ZaminShop without Anchor and confirm normal startup with fallback warning
- start with Anchor only and confirm scheduler migration and startup logs
- start with Anchor plus LuckPerms and test permission-gated shop access
- test async permission grant and revoke flows from command-driven purchase paths
- start with Anchor plus Vault and test Vault-backed shop transactions
- run on both Paper and Folia
- exercise shop actions that dispatch commands or reopen menus after delayed tasks
