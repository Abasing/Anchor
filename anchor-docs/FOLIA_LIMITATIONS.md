# Folia Limitations

Anchor has a Folia-aware architecture. That is the correct claim.

It is not a claim that every plugin on the server is safe on Folia.

## Important limits

- Anchor can provide proper global, async, region, and entity scheduler contexts.
- Anchor cannot rewrite unsafe scheduler assumptions inside other plugins.
- Anchor cannot guarantee that a third-party plugin marked `folia-supported: true` is actually safe in every code path.
- On non-Folia platforms, region and entity contexts intentionally degrade to global scheduling.

## What to watch for

- plugins that still call `BukkitScheduler` directly
- plugins missing a Folia declaration in `plugin.yml`
- plugins using hot reload tools
- async code that still touches world or entity state directly

## What Anchor helps with

- new code can target one scheduler API now
- migration away from direct scheduler hooks becomes incremental
- runtime diagnostics can flag likely compatibility problems earlier

## What still requires human validation

- cross-plugin interaction under load
- region ownership mistakes in consumer code
- entity lifecycle edge cases
- plugin stacks with mixed modern and legacy scheduler behavior
