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
- permission mutation for the actual shop purchase flow stayed direct in ZaminShop because Anchor originally lacked safe mutation support and world-aware permission checks
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
- consumer plugins with their own scheduler facade needed clear migration guidance

## Why Anchor stayed optional

ZaminShop already had mature fallback paths for schedulers, non-Vault economies,
and permission handling.

Making Anchor optional let the plugin prove the integration value without
forcing every server to install Anchor immediately.

## Runtime testing checklist

- start ZaminShop without Anchor and confirm normal startup with fallback warning
- start with Anchor only and confirm scheduler migration and startup logs
- start with Anchor plus LuckPerms and test permission-gated shop access
- start with Anchor plus Vault and test Vault-backed shop transactions
- run on both Paper and Folia
- exercise shop actions that dispatch commands or reopen menus after delayed tasks
