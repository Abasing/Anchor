# Production Readiness

Anchor is meant to reduce integration risk, not hide it.

## What Anchor guarantees

- Missing optional plugins do not hard-crash Anchor consumers.
- Every core service has a predictable availability state.
- Scheduler access stays behind one API surface instead of direct Paper or Folia classes.
- `/anchor doctor` and `/anchor metrics` expose runtime conditions in plain server terms.

## What Anchor does not guarantee

- Anchor does not make third-party plugins Folia-safe.
- Anchor does not guarantee that fallback behavior matches every server policy.
- Anchor does not guarantee that hot-reload tools keep runtime state valid.
- Anchor does not guarantee thread safety for code that ignores Anchor and touches Bukkit state from async work.

## Before calling it production-ready

- Test on plain Paper with no optional hooks.
- Test with the exact Vault, LuckPerms, PlaceholderAPI, and WorldGuard versions you plan to deploy.
- Run `/anchor doctor` after startup and after reconfiguration.
- Run the stress example on the same scheduler platform you plan to use in production.
- Validate your own consumer plugins against unavailable providers, fallback providers, and reload behavior.

## Recommended rollout order

1. Use Anchor in your own plugins first.
2. Validate on a Paper test server.
3. Validate on a Folia test server if you plan to support Folia.
4. Watch `/anchor metrics` during test load.
5. Only then start broader external adoption.

## Current status

Anchor is now in ecosystem validation and hardening.

The architecture is Folia-aware and the scheduler abstraction is real, but that is not the same thing as blanket production certification for every plugin stack.
