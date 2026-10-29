# Codex Working Notes

This file is the short operational memory for Codex and other coding agents.
For full context, read `docs/architecture.md` and `docs/development.md`.

## Project Snapshot

- Multi-version and multi-loader Gradle project orchestrated with **Stonecutter** (`0.9.8`) and **Architectury Loom** (`1.17.493`).
- Product name: **Open Parties and Claims: Warfare**.
- OPaC is the authority for parties and claims; this mod adds chunk wars, contested claims,
  raid windows, capture progress, war lives, fair-play Xaero map integration, and discord webhooks.
- Modules:
  - `common`: shared cross-loader codebase (Core, War, Xaero, and WarfareBridge API) preprocessing across versions `1.20.1`, `1.21.1`, and `26.1`.
  - `neoforge`: NeoForge platform layer targeting `1.21.1` and `26.1`.
  - `fabric`: Fabric platform layer targeting `1.20.1` and `1.21.1`.
  - `forge`: Forge platform layer targeting `1.20.1`.
- Protection features were extracted into companion mod `Open Parties and Claims: Better Protection`
  (`https://github.com/SvinPepe/opac-better-protection`).
- Output distribution artifacts:
  - `opac-warfare-1.20.1-forge.jar` (`:forge:1.20.1:remapJar`)
  - `opac-warfare-1.20.1-fabric.jar` (`:fabric:1.20.1:remapJar`)
  - `opac-warfare-1.21.1-neoforge.jar` (`:neoforge:1.21.1:remapJar`)
  - `opac-warfare-1.21.1-fabric.jar` (`:fabric:1.21.1:remapJar`)
  - `opac-warfare-26.1-neoforge.jar` (`:neoforge:26.1:assemble`)
- Active Stonecutter target: `"1.21.1"` in `stonecutter.gradle`.

## Read First

Before changing behavior, inspect the relevant files:

- Project root and build: `settings.gradle`, `build.gradle`, `gradle.properties`, `stonecutter.gradle`.
- Core services: `common/src/main/java/dev/customclaims/core/CoreServices.java`.
- War services: `common/src/main/java/dev/customclaims/war/WarServices.java`.
- Commands: `WarCommand.java`, `WarAdminCommand.java`.
- Configs: `CoreConfig.java`, `WarConfig.java`, `XaeroCompatConfig.java`.
- Platform abstraction: `dev.customclaims.core.platform.IPlatformHelper`, `IPermissionHelper`, `INetworkHelper`, `PlatformHelperHolder`.
- Warfare bridge API: `dev.customclaims.api.war.IWarfareBridge`, `dev.customclaims.api.war.WarfareBridgeHolder`.

## Safe Checks

Unit tests (runs on active version 1.21.1):

```powershell
.\gradlew.bat --no-daemon :common:1.21.1:test
```

Full matrix build:

```powershell
.\gradlew.bat --no-daemon :neoforge:1.21.1:remapJar :fabric:1.21.1:remapJar :fabric:1.20.1:remapJar :forge:1.20.1:remapJar :neoforge:26.1:assemble
```

Focused compile of all common targets:

```powershell
.\gradlew.bat --no-daemon :common:1.20.1:compileJava :common:1.21.1:compileJava :common:26.1:compileJava
```

## Module Ownership & Abstraction

- `common`:
  - Contains all shared domain logic (war management, sector wedge conquest, contested claims, raid windows, HUD, Discord webhooks, Xaero marker sync, OPaC adapters).
  - Holds `IWarfareBridge` and `WarfareBridgeHolder` under `dev.customclaims.api.war`.
  - Interfaces with platform loaders via `IPlatformHelper`, `IPermissionHelper`, and `INetworkHelper` in `dev.customclaims.core.platform`.
- `neoforge`:
  - `NeoForgePlatformHelper`, `NeoForgePermissionHelper`, `NeoForgeNetworkHelper`.
  - Mod entrypoint `OpacWarfareMod` for NeoForge 1.21.1 & 26.1.
- `fabric`:
  - `FabricPlatformHelper`, `FabricPermissionHelper` (supports LuckPerms via reflection and vanilla permission levels), `FabricNetworkHelper` (handles `ServerPlayNetworking`/`ClientPlayNetworking`).
  - Entrypoints `CustomClaimsFabricMod` and `CustomClaimsFabricClientMod`.
- `forge`:
  - `ForgePlatformHelper`, `ForgePermissionHelper`, `ForgeNetworkHelper` (using `NetworkRegistry.newSimpleChannel`).
  - Mod entrypoint `OpacWarfareForgeMod` for Minecraft Forge 1.20.1.

## WarfareBridge Status & Integration

- `IWarfareBridge` and `WarfareBridgeHolder` live in `common` under package `dev.customclaims.api.war`.
- `WarManager` registers its implementation (`this::isContestedWarChunk`) into `WarfareBridgeHolder` on startup.
- External companion mods, specifically `Open Parties and Claims: Better Protection`, query `WarfareBridgeHolder.isContestedWarChunk(level, chunkPos)` to determine if a chunk is in active siege/war, allowing destruction in contested zones while protecting peaceful claims.
- The `IWarfareBridge` contract is a permanent public integration boundary; do not alter method signatures without backwards compatibility.

## Do Not Break

- OPaC remains the source of truth for party membership and claim ownership.
- Active contested chunks must preserve original claim snapshots for restore paths.
- Contested access is only for attacker and defender sides.
- Xaero marker sync and map war-start must stay fair-play scoped; do not expose global claim-owner map data. Map war-start packets must remain server validated for permission, current dimension, configured distance, and normal war target rules.
- Client-only code must stay behind client-side checks.
- Runtime data stays under `world/customclaims/`.
- `WarfareBridgeHolder` must remain safe to call even if `customclaims-war` is not present (defaults to false).
- `stonecutter.active` must remain `"1.21.1"` in `stonecutter.gradle` for VCS commit baseline.

## Docs Maintenance

- Update `README.md` when the public entrypoint or install story changes.
- Update `docs/server-admin.md` for operator-visible behavior changes.
- Update `docs/configuration.md` for config key/default changes. Keep the defaults table exact; do not leave stale values after code changes.
- Update `docs/architecture.md` for module, service, persistence, or compat boundary changes.
- Update `docs/development.md` for build, CI, or workflow changes.
- Update `docs/compatibility.md` for loader, Minecraft, and companion mod compatibility.
