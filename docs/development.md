# Development

This repository uses Gradle, NeoForge userdev, Java `21`, and a `1.21.1` compile baseline, with architectural support for multi-loader (Fabric + NeoForge via Architectury) and multi-version (Minecraft 1.21.1 and 26.1 via Stonecutter).

## Setup

Required locally:

- JDK `21`
- Git
- The Gradle wrapper included in this repository

The build resolves Minecraft/NeoForge and mod dependencies through configured Maven repositories, including Modrinth Maven for Open Parties and Claims.

This repository compiles against `Minecraft 1.21.1 + NeoForge 21.1.232`, while metadata allows experimental same-jar probes on Minecraft `[1.21.1,26.0)` and NeoForge `[21.1.232,26.0)`.

Important version properties live in `gradle.properties`:

```properties
minecraft_version=1.21.1
minecraft_version_range=[1.21.1,26.0)
neo_version=21.1.232
neo_version_range=[21.1.232,26.0)
opc_version=neoforge-1.21.1-0.27.5
mod_version=2.0.0
```

## Build and Test Commands

### CI-Parity Build & Test

To build the release jar and execute the full test suite:

```powershell
.\gradlew.bat --no-daemon :opac-warfare:jar test
```

On Linux/macOS:

```sh
./gradlew --no-daemon :opac-warfare:jar test
```

The resulting distribution artifact is located at:

```text
opac-warfare/build/libs/opac-warfare-<version>.jar
```

### Focused Development Commands

Compile all functional warfare modules (single-worker, quiet output):

```powershell
.\gradlew.bat --no-daemon "-Dorg.gradle.workers.max=1" :customclaims-core:compileJava :customclaims-war:compileJava :customclaims-xaero:compileJava -q
```

Build only the distribution jar:

```powershell
.\gradlew.bat --no-daemon :opac-warfare:jar
```

Run all unit tests across subprojects:

```powershell
.\gradlew.bat --no-daemon test
```

Run tests for specific modules:

```powershell
.\gradlew.bat --no-daemon :customclaims-core:test :customclaims-war:test
```

## Release Checklist

Before releasing a new version:

1. Run the CI build and test command:
   ```powershell
   .\gradlew.bat --no-daemon :opac-warfare:jar test
   ```
2. Smoke-test the built jar on the baseline `Minecraft 1.21.1 + NeoForge 21.1.232` with OPaC:
   - Server boots cleanly without mixin, classloading, packet, or config errors.
   - Player commands `/war start`, `/war status`, `/war list`, and `/war near` function as expected.
   - Admin commands under `/waradmin` (`list`, `stop`, `skipprep`, `setprogress`) execute properly.
   - Default side cooldowns block a second outgoing declaration for `2` hours and a second incoming declaration against the defending side for `1` hour.
   - Raid windows and capture progress tick properly.
   - War lives decrement on death and end war upon exhaustion.
   - Xaero war waypoint sync works on a compatible client, and right-click war start behaves as expected.
3. If testing alongside the companion mod **[Open Parties and Claims: Better Protection](https://github.com/SvinPepe/opac-better-protection)**:
   - Verify `WarfareBridge` correctly allows contraptions, cannons, and explosions inside contested war chunks while preserving claim protection elsewhere.

## Multi-Version and Multi-Loader Workflows

For detailed documentation, see [Multi-Loader & Multi-Version Architecture](multiversion-multiloader.md) and `stonecutter.gradle`.

Stonecutter allows switching target versions during development by setting `stonecutter.active "<version>"` in `stonecutter.gradle` or using the Stonecutter IntelliJ IDEA plugin version switcher.


