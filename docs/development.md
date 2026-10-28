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

## CI-Parity Build

GitHub Actions runs:

```sh
./gradlew --no-daemon build :opac-warfare:jar test
```

On Windows, use:

```powershell
.\gradlew.bat --no-daemon build :opac-warfare:jar test
```

The uploaded release artifact comes from:

```text
opac-warfare/build/libs/opac-warfare-*.jar
```

## Release Checklist

Before release, run:

```powershell
.\gradlew.bat --no-daemon build :opac-warfare:jar test
```

Smoke-test the built jar on the baseline `Minecraft 1.21.1 + NeoForge 21.1.232` with OPaC:

- server boots without mixin, classloading, packet, or config errors;
- `/war status`, `/war start`, `/war near`, and `/waradmin` work;
- default side cooldowns block a second outgoing declaration for `2` hours and a second incoming declaration against the defending side for `1` hour;
- raid windows and capture progress tick properly;
- war lives decrement on death and end war upon exhaustion;
- Xaero war waypoint sync works on a compatible client.

## Focused Commands

Compile all functional warfare modules:

```powershell
.\gradlew.bat --no-daemon "-Dorg.gradle.workers.max=1" :customclaims-core:compileJava :customclaims-war:compileJava :customclaims-xaero:compileJava -q
```

Build jars:

```powershell
.\gradlew.bat --no-daemon :opac-warfare:jar
```

Run test suite:

```powershell
.\gradlew.bat --no-daemon test
```

## Multi-Version and Multi-Loader Workflows

For detailed documentation, see [Multi-Loader & Multi-Version Architecture](multiversion-multiloader.md) and `stonecutter.gradle`.

Stonecutter allows switching target versions during development:

```powershell
.\gradlew.bat SetActiveVersion --version 1.21.1
```

Or building across configured version matrices:

```powershell
.\gradlew.bat chiseledBuild
```
