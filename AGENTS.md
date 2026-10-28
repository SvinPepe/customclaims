# Codex Working Notes

This file is the short operational memory for Codex and other coding agents.
For full context, read `docs/architecture.md` and `docs/development.md`.

## Project Snapshot

- Gradle multi-module project with Minecraft `1.21.1` compile baseline and Java `21`.
- Product name: **Open Parties and Claims: Warfare**.
- OPaC is the authority for parties and claims; this mod adds chunk wars, contested claims,
  raid windows, capture progress, war lives, fair-play Xaero map integration, and discord webhooks.
- Modules: strictly `customclaims-core`, `customclaims-war`, `customclaims-xaero`, and `opac-warfare`.
  Protection features were extracted into the companion mod `Open Parties and Claims: Better Protection`
  (`https://github.com/SvinPepe/opac-better-protection`).
- Server artifact:
  - `opac-warfare/build/libs/opac-warfare-*.jar` (Warfare 2.0: Core, War, Xaero)
- Compatibility anchor: baseline `Minecraft 1.21.1 + NeoForge 21.1.232`;
  metadata allows experimental Minecraft `[1.21.1,26.0)` and NeoForge `[21.1.232,26.0)` probes.
  Multi-loader (Fabric + NeoForge via Architectury) and multi-version (via Stonecutter) targets are supported in the project architecture.

## Read First

Before changing behavior, inspect the relevant files:

- Modules and metadata: `settings.gradle`, `build.gradle`, `gradle.properties`.
- Core services: `customclaims-core/src/main/java/dev/customclaims/core/CoreServices.java`.
- War services: `customclaims-war/src/main/java/dev/customclaims/war/WarServices.java`.
- Commands: `WarCommand.java`, `WarAdminCommand.java`.
- Configs: `CoreConfig.java`, `WarConfig.java`, `XaeroCompatConfig.java`.
- Warfare bridge API: `dev.customclaims.api.war.IWarfareBridge`, `dev.customclaims.api.war.WarfareBridgeHolder`.

## Safe Checks

CI parity:

```powershell
.\gradlew.bat --no-daemon :opac-warfare:jar test
```

Focused compile:

```powershell
.\gradlew.bat --no-daemon "-Dorg.gradle.workers.max=1" :customclaims-core:compileJava :customclaims-war:compileJava :customclaims-xaero:compileJava -q
```

Docs-only changes do not require a full Gradle build, but command/config docs
must be checked against Java source.

## Module Ownership

- `customclaims-core`: OPaC adapters, side identity, territory status,
  permissions (NeoForge PermissionAPI + LuckPerms), storage, logs, common messages, rollback interface,
  and the external integration bridge API (`IWarfareBridge`, `WarfareBridgeHolder`).
- `customclaims-war`: sector war conquest wedge, contested ownership, anti-zerg capture, glowing effect, lives,
  daily start/accepted limits, raid windows, HUD, discord webhooks, `/war`, `/waradmin`, and registers the active war chunk provider into `WarfareBridgeHolder`.
- `customclaims-xaero`: fair-play war marker sync, client waypoint/overlay
  behavior, and Xaero World Map right-click war-start requests.
- `opac-warfare`: bundles `customclaims-core`, `customclaims-war`, `customclaims-xaero`.

## WarfareBridge Status & Integration

- `IWarfareBridge` and `WarfareBridgeHolder` live in `customclaims-core` under package `dev.customclaims.api.war`.
- `customclaims-war` (`WarManager`) registers its implementation (`this::isContestedWarChunk`) into `WarfareBridgeHolder` on startup.
- External companion mods, specifically `Open Parties and Claims: Better Protection`, query `WarfareBridgeHolder.isContestedWarChunk(level, chunkPos)` to determine if a chunk is in active siege/war, allowing destruction in contested zones while protecting peaceful claims.
- The `IWarfareBridge` contract is a permanent public integration boundary; do not alter method signatures without backwards compatibility.

## Do Not Break

- OPaC remains the source of truth for party membership and claim ownership.
- Active contested chunks must preserve original claim snapshots for restore
  paths.
- Contested access is only for attacker and defender sides.
- Xaero marker sync and map war-start must stay fair-play scoped; do not expose
  global claim-owner map data. Map war-start packets must remain server
  validated for permission, current dimension, configured distance, and normal
  war target rules.
- Do not add untested versions without verified compatibility probes.
- Client-only code must stay behind client-side checks.
- Runtime data stays under `world/customclaims/`.
- `WarfareBridgeHolder` must remain safe to call even if `customclaims-war` is not present (defaults to false).

## Docs Maintenance

- Update `README.md` when the public entrypoint or install story changes.
- Update `docs/server-admin.md` for operator-visible behavior changes.
- Update `docs/configuration.md` for config key/default changes. Keep the
  defaults table exact; do not leave stale values after code changes.
- Update `docs/architecture.md` for module, service, persistence, or compat
  boundary changes.
- Update `docs/development.md` for build, CI, or workflow changes.
- Update `docs/compatibility.md` for loader, Minecraft, and companion mod compatibility.

