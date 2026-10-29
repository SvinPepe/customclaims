# Development

This repository uses Gradle, Stonecutter (`0.9.8`), Architectury Loom (`1.17.493`), and supports **Minecraft 1.20.1, 1.21.1, and 26.1** across **Forge, NeoForge, and Fabric**.

## Setup

Required locally:

- JDK `21` (default baseline for 1.21.1 development); JDK 17 toolchain for 1.20.1 and JDK 25 toolchain for 26.1 are automatically handled by Gradle Toolchains / Foojay resolver.
- Git
- Gradle wrapper (`gradlew` / `gradlew.bat`)

The active Stonecutter target for source editing is set to `"1.21.1"` in `stonecutter.gradle`.

## Target Matrix & Distribution Jars

| Target | Gradle Task | Output Artifact |
| --- | --- | --- |
| **1.20.1 Forge** | `:forge:1.20.1:remapJar` | `forge/versions/1.20.1/build/libs/opac-warfare-1.20.1-forge.jar` |
| **1.20.1 Fabric** | `:fabric:1.20.1:remapJar` | `fabric/versions/1.20.1/build/libs/opac-warfare-1.20.1-fabric.jar` |
| **1.21.1 NeoForge** | `:neoforge:1.21.1:remapJar` | `neoforge/versions/1.21.1/build/libs/opac-warfare-1.21.1-neoforge.jar` |
| **1.21.1 Fabric** | `:fabric:1.21.1:remapJar` | `fabric/versions/1.21.1/build/libs/opac-warfare-1.21.1-fabric.jar` |
| **26.1 NeoForge** | `:neoforge:26.1:assemble` | `neoforge/versions/26.1/build/libs/opac-warfare-26.1-neoforge.jar` |

## Build and Test Commands

### Run Unit Tests

```powershell
.\gradlew.bat --no-daemon :common:1.21.1:test
```

On Linux/macOS:

```sh
./gradlew --no-daemon :common:1.21.1:test
```

### Build Full Matrix

To build all 5 distribution jars across all loaders and versions:

```powershell
.\gradlew.bat --no-daemon :neoforge:1.21.1:remapJar :fabric:1.21.1:remapJar :fabric:1.20.1:remapJar :forge:1.20.1:remapJar :neoforge:26.1:assemble
```

On Linux/macOS:

```sh
./gradlew --no-daemon :neoforge:1.21.1:remapJar :fabric:1.21.1:remapJar :fabric:1.20.1:remapJar :forge:1.20.1:remapJar :neoforge:26.1:assemble
```

### Focused Target Commands

Build Forge 1.20.1:
```powershell
.\gradlew.bat --no-daemon :forge:1.20.1:remapJar
```

Build Fabric 1.20.1:
```powershell
.\gradlew.bat --no-daemon :fabric:1.20.1:remapJar
```

Build Fabric 1.21.1:
```powershell
.\gradlew.bat --no-daemon :fabric:1.21.1:remapJar
```

Build NeoForge 1.21.1:
```powershell
.\gradlew.bat --no-daemon :neoforge:1.21.1:remapJar
```

Build NeoForge 26.1:
```powershell
.\gradlew.bat --no-daemon :neoforge:26.1:assemble
```

Compile all common Stonecutter targets:
```powershell
.\gradlew.bat --no-daemon :common:1.20.1:compileJava :common:1.21.1:compileJava :common:26.1:compileJava
```

## Release Checklist

Before releasing a new version:

1. Run the test suite:
   ```powershell
   .\gradlew.bat --no-daemon :common:1.21.1:test
   ```
2. Build the full matrix of distribution jars:
   ```powershell
   .\gradlew.bat --no-daemon :neoforge:1.21.1:remapJar :fabric:1.21.1:remapJar :fabric:1.20.1:remapJar :forge:1.20.1:remapJar :neoforge:26.1:assemble
   ```
3. Verify that `stonecutter.active` is set to `"1.21.1"` in `stonecutter.gradle`.
4. Ensure public contracts (`dev.customclaims.api.war.IWarfareBridge`, `WarfareBridgeHolder`) remain stable.
