# Contributing

## Scope

Anchor is an ecosystem abstraction layer. Contributions should improve:

- compatibility
- scheduler safety
- diagnostics
- public API clarity
- migration experience

Avoid turning Anchor into a generic utility dump.

## Public API rules

- Treat `me.zamin.anchor.api.*` as the stable consumer surface.
- Prefer refining internals over casually changing the public API.
- If a public API change is necessary before 1.0, document it in `CHANGELOG.md`.

## Development

Build with Java 17+ and run:

```powershell
$env:JAVA_HOME='F:\Zamin\InstalledSoftNOTOUCH'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn -DskipTests package
```

## Documentation expectations

Public API additions must include:

- JavaDocs
- example usage when appropriate
- migration notes if they replace older patterns
- diagnostics notes when the feature affects runtime safety
