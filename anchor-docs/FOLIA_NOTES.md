# Folia Notes

Anchor does not claim fake Folia support.

PaperMC documents that adding `folia-supported: true` is not enough. Folia has no single main thread and requires correct use of global, region, async, and entity schedulers.

Current Anchor state:

- Scheduler API is designed for future Folia-aware expansion.
- Runtime implementation is Bukkit/Paper-first.
- No `folia-supported: true` flag is advertised yet.

Reference: https://docs.papermc.io/paper/dev/folia-support/
