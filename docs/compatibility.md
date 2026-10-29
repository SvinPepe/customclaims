# Compatibility Policy

This project publishes dedicated distribution jars for supported loaders and Minecraft versions:

- `forge/versions/1.20.1/build/libs/opac-warfare-1.20.1-forge.jar`
- `fabric/versions/1.20.1/build/libs/opac-warfare-1.20.1-fabric.jar`
- `neoforge/versions/1.21.1/build/libs/opac-warfare-1.21.1-neoforge.jar`
- `fabric/versions/1.21.1/build/libs/opac-warfare-1.21.1-fabric.jar`
- `neoforge/versions/26.1/build/libs/opac-warfare-26.1-neoforge.jar`

## Compatibility Matrix

| Minecraft Version | Mod Loader | Loader Version | Java Version | OPaC Compatibility | Status |
| --- | --- | --- | --- | --- | --- |
| **1.20.1** | **Forge** | `47.3.0`+ | Java 17 | `forge-1.20.1-0.21.3`+ | **Supported** |
| **1.20.1** | **Fabric** | `0.16.14`+ | Java 17 | `fabric-1.20.1-0.21.3`+ | **Supported** |
| **1.21.1** | **NeoForge** | `21.1.232`+ | Java 21 | `neoforge-1.21.1-0.27.5`+ | **Supported** |
| **1.21.1** | **Fabric** | `0.16.14`+ | Java 21 | `fabric-1.21.1-0.27.5`+ | **Supported** |
| **26.1** | **NeoForge** | `26.1.2.112`+ | Java 25 | `neoforge-26.3-0.31.6`+ | **Supported** |

### Optional Integrations

- **Xaero's Minimap**: Supported on client for temporary fair-play war waypoints.
- **Xaero's World Map**: Supported on client for waypoints and right-click "Start war here" context menu action.
- **OPaC: Better Protection**: Supported companion mod (Create, CBC, explosions, `/claimrules`). Integrates via `WarfareBridge`.

## Multi-Loader and Multi-Version Architecture

- **Stonecutter (`0.9.8`)**: Preprocesses and maintains source branches across `1.20.1`, `1.21.1`, and `26.1`.
- **Architectury Loom (`1.17.493`)**: Builds Forge and Fabric jars with automated remapping and compile-time mod dependency injection.
- **Platform Abstractions**: The `common` module interacts with platform-specific subsystems (permissions, packet networking, mod checks, config directories) through `IPlatformHelper`, `IPermissionHelper`, and `INetworkHelper`.

## Companion Mod Integration: Better Protection

Starting in Warfare 2.0, claim protection rules and mechanics (Create contraption assembly, drills, saws, CBC/Clockwork cannon projectile impacts, explosion protection, and `/claimrules`) have been extracted into the companion mod:
👉 **[Open Parties and Claims: Better Protection](https://github.com/SvinPepe/opac-better-protection)**

`opac-warfare` and `opac-better-protection` integrate dynamically through `dev.customclaims.api.war.WarfareBridgeHolder`. Neither mod has a hard runtime dependency on the other:
- If both mods are installed, `opac-better-protection` automatically allows block damage and contraption interaction in contested war chunks while preserving claim protection elsewhere.
- If only `opac-warfare` is installed, warfare runs without custom protection overrides.
