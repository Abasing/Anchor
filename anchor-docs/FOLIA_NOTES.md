# Folia Notes

Anchor does not claim fake Folia support.

PaperMC documents that adding `folia-supported: true` is not enough. Folia has no single main thread and requires correct use of global, region, async, and entity schedulers.

Current Anchor state:

- Scheduler API exposes global, async, region, and entity execution contexts.
- Runtime selects a Bukkit adapter on Paper/Spigot and a Folia adapter on Folia.
- Public API stays platform-neutral and does not expose Folia classes directly.
- Anchor still avoids fake support claims beyond the runtime behavior it actually provides.

Reference: https://docs.papermc.io/paper/dev/folia-support/
