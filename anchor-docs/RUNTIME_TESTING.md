# Runtime Testing

This document defines manual runtime checks for Anchor before broader adoption.

## Paper without optional hooks

Setup:
- Start Paper with only `anchor-plugin` and `anchor-test-plugin`.

Commands:
- `/anchor status`
- `/anchor hooks`
- `/anchor doctor`
- `/anchortest`

Expected:
- economy unavailable fallback
- permissions Bukkit fallback
- placeholders internal fallback
- regions fallback based on config
- scheduler reports Bukkit

Must not happen:
- plugin startup crash
- missing class errors
- command failures due to missing optional plugins

## Paper with Vault only

Setup:
- Add Vault and a compatible economy plugin.

Commands:
- `/anchor status`
- `/anchortest`

Expected:
- economy provider reports Vault-backed provider
- permission service may still be fallback if no permissions bridge is present

Must not happen:
- economy calls crashing when permissions provider is missing

## Paper with Vault + LuckPerms

Setup:
- Add Vault, compatible economy plugin, and LuckPerms.

Commands:
- `/anchor status`
- `/anchor hooks`

Expected:
- economy active
- permissions active through LuckPerms
- Vault permissions hook may appear as lower-priority fallback or inactive path

Must not happen:
- ambiguous provider state in status output

## Paper with PlaceholderAPI

Setup:
- Add PlaceholderAPI.

Commands:
- `/anchortest`

Expected:
- placeholder provider reports PlaceholderAPI
- internal placeholders still parse cleanly

Must not happen:
- raw `{player}` tokens remaining when the internal resolver should handle them

## Paper with WorldGuard

Setup:
- Add WorldGuard v7+.

Commands:
- `/anchor status`
- `/anchortest`

Expected:
- regions provider reports WorldGuard
- region checks succeed without fallback warning

Must not happen:
- region API throwing adapter exceptions

## Folia without optional hooks

Setup:
- Start Folia with `anchor-plugin` and `anchor-test-plugin`.

Commands:
- `/anchor doctor`
- run example plugin commands if added locally

Expected:
- scheduler platform reports Folia
- doctor warns about Folia-aware scheduling where relevant
- fallback services still work

Must not happen:
- fake universal main-thread assumptions
- startup crash from missing optional plugins

## Folia with Anchor examples

Setup:
- Start Folia with Anchor and the example jars from `examples/*`.

Commands:
- execute the example plugin commands or trigger methods through test harness plugins

Expected:
- region and entity scheduler demos execute
- delayed and repeating tasks can be cancelled
- completion futures resolve

Must not happen:
- direct Folia API leakage into consumer plugins

## Missing dependencies

Setup:
- remove Vault, PlaceholderAPI, LuckPerms, and WorldGuard

Commands:
- `/anchor hooks`
- `/anchor doctor`

Expected:
- actionable warnings with cause and recommended fix

Must not happen:
- null pointer failures from missing hooks

## Reload behavior

Setup:
- server running with Anchor loaded

Commands:
- `/anchor reload`
- `/anchor status`

Expected:
- services rebind successfully
- GUI listener and scheduler state reinitialize cleanly

Must not happen:
- duplicate listener behavior
- API becoming unbound after reload

## Status and hook commands

Setup:
- any supported runtime

Commands:
- `/anchor status`
- `/anchor hooks`

Expected:
- provider names
- service states
- scheduler adapter details
- hook timing data

Must not happen:
- vague “enabled/disabled” output with no context

## Doctor command

Setup:
- any supported runtime

Commands:
- `/anchor doctor`

Expected:
- severity
- problem
- likely cause
- recommended fix
- compatibility scan notes

Must not happen:
- warnings without actionable next steps
