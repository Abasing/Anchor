# Threading Model

## Paper model

Traditional Paper and Spigot plugin code usually assumes a single main server thread for world access and a separate async scheduler for off-thread work.

## Folia model

Folia splits the server into independently ticking regions. There is no single universal main thread for all world state. Region-owned and entity-owned work must be scheduled in the correct ownership context.

## Why direct BukkitScheduler assumptions are unsafe

Code that assumes all world access can be marshalled through one global scheduler becomes unsafe on Folia. Region ownership matters.

## When to use `global()`

Use `global()` for server-global work such as:

- console-owned commands
- global state not tied to one entity or location
- coordination work that is not region-owned

## When to use `async()`

Use `async()` for:

- CPU-heavy pure Java work
- I/O work
- lookups that do not touch Bukkit world state directly

Do not touch unsafe Bukkit world state from async tasks.

## When to use `region(location)`

Use `region(location)` when work is owned by a location, chunk, block, or region.

Examples:

- block edits
- chunk-adjacent state checks
- world interactions tied to a location

## When to use `entity(entity)`

Use `entity(entity)` when work is owned by an entity and should follow that entity safely across teleports or region movement.

Examples:

- player inventory or entity state updates
- entity teleport follow-up logic
- delayed player feedback tied to that player

## What Anchor guarantees

- a stable scheduler abstraction
- no Folia classes in the public API
- Bukkit/Paper fallback behavior when region/entity schedulers are unavailable
- task handles, completion futures, and cancellation support

## What Anchor does not guarantee

- that third-party hooks are thread-safe outside their intended scheduler context
- that async tasks may safely touch Bukkit world state
- that every external plugin is Folia-safe just because Anchor is present
