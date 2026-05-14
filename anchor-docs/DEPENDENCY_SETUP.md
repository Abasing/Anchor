# Dependency Setup

## Plugin developers

Depend on `anchor-api`, not `anchor-plugin`.

```xml
<dependency>
    <groupId>me.zamin</groupId>
    <artifactId>anchor-api</artifactId>
    <version>1.2.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

Why:

- `anchor-api` is the stable compile-time contract
- `anchor-plugin` is the installed runtime plugin
- shading `anchor-plugin` into your own plugin defeats the shared-runtime model

## plugin.yml

Anchor should normally be optional:

```yaml
softdepend:
  - Anchor
```

If your plugin cannot run meaningfully without Anchor, document that clearly and use `depend` instead. Most migrations should start with `softdepend` and keep fallback behavior where practical.

## Server owners

Install the runtime plugin jar:

- `anchor-plugin-1.2.0-SNAPSHOT.jar`

Do not install `anchor-api` on the server by itself. It is a compile-time artifact for developers.

## Local development

Build everything:

```powershell
$env:JAVA_HOME='F:\Zamin\InstalledSoftNOTOUCH'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn clean package
```

Install the API to your local Maven repository:

```powershell
$env:JAVA_HOME='F:\Zamin\InstalledSoftNOTOUCH'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn -pl anchor-api -am -DskipTests install
```
