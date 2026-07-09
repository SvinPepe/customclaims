# Development

This repository uses Gradle, NeoForge userdev, Java `21`, and a `1.21.1`
compile baseline.

## Setup

Required locally:

- JDK `21`
- Git
- The Gradle wrapper included in this repository

The build resolves Minecraft/NeoForge and mod dependencies through configured
Maven repositories, including Modrinth Maven for Open Parties and Claims,
Create, Create Big Cannons, and related compat dependencies.
Aeronautics/Offroad compat uses the local bundled jar in `libs/` for
development runtime coverage.

This repository intentionally keeps one official jar. It compiles against
`Minecraft 1.21.1 + NeoForge 21.1.232`, while metadata allows experimental
same-jar probes on Minecraft `[1.21.1,1.27)` and NeoForge `[21.1.232,27.0)`.
Do not add multi-version Gradle targets or widen those ranges without updating
[Compatibility](compatibility.md) and smoke-test expectations.

Important version properties live in `gradle.properties`:

```properties
minecraft_version=1.21.1
minecraft_version_range=[1.21.1,1.27)
neo_version=21.1.232
neo_version_range=[21.1.232,27.0)
opc_version=neoforge-1.21.1-0.27.5
create_version=mc1.21.1-6.0.9
cbc_version=5.11.7
rpl_version=2.1.2
mod_version=1.6.3
```

## CI-Parity Build

GitHub Actions runs:

```sh
./gradlew --no-daemon build :opac-warfare:jar
```

On Windows, use:

```powershell
.\gradlew.bat --no-daemon build :opac-warfare:jar
```

The uploaded release artifact comes from:

```text
opac-warfare/build/libs/opac-warfare-*.jar
```

## Release Checklist

Before release, run:

```powershell
.\gradlew.bat --no-daemon build :opac-warfare:jar
```

Smoke-test the built jar on the baseline `Minecraft 1.21.1 + NeoForge 21.1.232` with OPaC:

- server boots without mixin, classloading, packet, or config errors;
- `/war status`, `/war start`, and `/claimrules create status` work;
- daily territory fight limits block the 6th successful outgoing start when the
  attacker default is `5` and the 11th successful incoming start when the
  defender default is `10`;
- protection blocks foreign peaceful interactions;
- Create machine protection works when Create is installed;
- Aeronautics/Offroad bore protection works when Aeronautics/Offroad is
  installed;
- Xaero war waypoint names and cleanup work on a compatible client.

NeoForge `21.x` through `26.x` candidates are probes only. Install the same jar
there, run the same smoke checks, and document the target as unsupported if boot
or gameplay checks fail.

## Focused Commands

Compile all functional modules:

```powershell
.\gradlew.bat --no-daemon "-Dorg.gradle.workers.max=1" :customclaims-core:compileJava :customclaims-war:compileJava :customclaims-protection:compileJava :customclaims-create:compileJava :customclaims-aeronautics:compileJava :customclaims-big-cannons:compileJava :customclaims-xaero:compileJava -q
```

Build functional module jars:

```powershell
.\gradlew.bat --no-daemon "-Dorg.gradle.workers.max=1" :customclaims-core:jar :customclaims-war:jar :customclaims-protection:jar :customclaims-create:jar :customclaims-aeronautics:jar :customclaims-big-cannons:jar :customclaims-xaero:jar -q
```

Build the distribution jar:

```powershell
.\gradlew.bat --no-daemon :opac-warfare:jar
```

If Gradle has just downloaded plugins or a worker daemon is stuck, stop daemons:

```powershell
.\gradlew.bat --stop
```

## Source Layout

Each functional module keeps normal Java sources under:

```text
<module>/src/main/java/dev/customclaims/<area>/
```

Resources live under:

```text
<module>/src/main/resources/
```

Generated resources are ignored by git through `**/src/generated/`.

## Working Rules

- Prefer existing services over adding logic directly to event handlers.
- Keep command classes responsible for parsing, permission checks, and messages;
  move behavior into services.
- Keep config keys stable unless a migration or release note is planned.
- Do not add hard dependencies from optional compat modules to absent target
  mods without a mod-load guard; Aeronautics/Offroad hooks must remain optional.
- Keep client-only UI, keybind, overlay, and waypoint code behind client-side
  checks.
- Update `docs/configuration.md` whenever a config key, default, or meaning
  changes.
- Update `docs/server-admin.md` whenever a command, permission, runtime file, or
  operator-visible behavior changes.
- Update `docs/architecture.md` when module boundaries, service ownership, or
  persistence changes.

## Testing Notes

There are no conventional unit test source sets in the repository right now.
Use focused Gradle compile/jar tasks while developing and run the CI-parity
command before release.

For docs-only changes, verify:

- module names match `settings.gradle`;
- commands match `WarCommand`, `WarAdminCommand`, and `ClaimRulesCommand`;
- config keys match `CoreConfig`, `WarConfig`, `ProtectionConfig`, and
  `XaeroCompatConfig`;
- Markdown links resolve.

For gameplay changes, test in a NeoForge server run with OPaC installed and
exercise the affected commands or event paths in-game. For Aeronautics/Offroad,
test Borehead Bearing + Rock Cutting Wheel mining against protected and allowed
claims.
