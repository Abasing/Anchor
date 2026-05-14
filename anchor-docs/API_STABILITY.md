# API Stability

## Public stable API

`me.zamin.anchor.api.*` is the public API for plugin developers.

This is the package tree consumers should compile against.

## Internal API

`me.zamin.anchor.internal.*` is not public API.

It may change freely between versions without downstream compatibility guarantees.

## Pre-1.0 policy

Before Anchor 1.0 stable:

- breaking public API changes are allowed
- every breaking change must be documented in `CHANGELOG.md`
- examples and migration docs must be updated with the new contract

## Post-1.0 policy

After Anchor 1.0 stable:

- breaking public API changes require a major version bump
- minor and patch releases should preserve source compatibility wherever possible
