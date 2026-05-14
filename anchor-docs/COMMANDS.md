# Commands

## `/anchor status`

Shows the active runtime summary:

- Anchor version
- server platform
- selected scheduler platform
- loaded providers

## `/anchor hooks`

Shows hook status for integrations such as:

- Vault economy
- Vault permissions
- LuckPerms
- PlaceholderAPI
- WorldGuard

## `/anchor doctor`

Shows actionable diagnostics:

- missing plugin hooks
- fallback services
- scheduler environment
- compatibility warnings
- startup validation issues

This is the first command to run when something looks wrong.

## `/anchor metrics`

Shows lightweight runtime metrics such as:

- adapter load timings
- hook lookup timings
- doctor scan timing
- scheduler counters and timings

## `/anchor reload`

Reloads Anchor configuration and runtime state where supported.

Do not treat reload as a substitute for full restart validation on complex production servers.
