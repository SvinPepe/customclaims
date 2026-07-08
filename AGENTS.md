# Codex Working Notes

This file is the short operational memory for Codex and other coding agents.
For full context, read `docs/architecture.md` and `docs/development.md`.

## Project Snapshot

- Gradle multi-module NeoForge project for Minecraft `1.21.1` and Java `21`.
- Product name: **Open Parties and Claims: Warfare**.
- OPaC is the authority for parties and claims; this repo adds wars,
  protection, and compat integrations.
- Normal server artifact: `opac-warfare/build/libs/opac-warfare-*.jar`.
- Compatibility anchor: one official jar for `Minecraft 1.21.1 + NeoForge
  21.1.x`; do not promise older/newer Minecraft, `26.x`, Forge, Fabric, or
  Quilt from this artifact.

## Read First

Before changing behavior, inspect the relevant files:

- Modules and metadata: `settings.gradle`, `build.gradle`, `gradle.properties`.
- Core services: `customclaims-core/src/main/java/dev/customclaims/core/CoreServices.java`.
- War services: `customclaims-war/src/main/java/dev/customclaims/war/WarServices.java`.
- Protection services: `customclaims-protection/src/main/java/dev/customclaims/protection/ProtectionServices.java`.
- Commands: `WarCommand.java`, `WarAdminCommand.java`, `ClaimRulesCommand.java`.
- Configs: `CoreConfig.java`, `WarConfig.java`, `ProtectionConfig.java`,
  `XaeroCompatConfig.java`.

## Safe Checks

CI parity:

```powershell
.\gradlew.bat --no-daemon build :opac-warfare:jar
```

Focused compile:

```powershell
.\gradlew.bat --no-daemon "-Dorg.gradle.workers.max=1" :customclaims-core:compileJava :customclaims-war:compileJava :customclaims-protection:compileJava :customclaims-create:compileJava :customclaims-aeronautics:compileJava :customclaims-big-cannons:compileJava :customclaims-xaero:compileJava -q
```

Docs-only changes do not require a full Gradle build, but command/config docs
must be checked against Java source.

## Module Ownership

- `customclaims-core`: OPaC adapters, side identity, territory status,
  permissions, storage, logs, common messages, rollback interface.
- `customclaims-war`: war lifecycle, contested ownership, capture, lives,
  raid windows, HUD, notifications, `/war`, `/waradmin`.
- `customclaims-protection`: claim rules, explosion/storage/villager/Wither
  protection, foreign interaction limits, claim-rule GUI payloads,
  `/claimrules`.
- `customclaims-create`: optional Create hooks.
- `customclaims-aeronautics`: optional Aeronautics/Offroad bore mining hooks.
- `customclaims-big-cannons`: optional Create Big Cannons hooks.
- `customclaims-xaero`: fair-play war marker sync, client waypoint/overlay
  behavior, and Xaero World Map right-click war-start requests.
- `opac-warfare`: single-jar distribution; keep it bundling functional modules.

## Do Not Break

- OPaC remains the source of truth for party membership and claim ownership.
- Active contested chunks must preserve original claim snapshots for restore
  paths.
- Contested access is only for attacker and defender sides.
- Xaero marker sync and map war-start must stay fair-play scoped; do not expose
  global claim-owner map data. Map war-start packets must remain server
  validated for permission, current dimension, configured distance, and normal
  war target rules.
- Optional Create, Aeronautics/Offroad, and CBC integrations must stay safe when
  target mods are not loaded.
- Do not add multi-version Gradle targets, widen `minecraft_version_range`, or
  claim adjacent-version support without the compatibility probe in
  `docs/compatibility.md`.
- Client-only code must stay behind client-side checks.
- Runtime data stays under `world/customclaims/`.

## Known TODO Areas

- Rollback is currently `NoopRollbackService`; persisted rollback is future
  work.
- `CaptureBoostItemService` has a placeholder for configured boost items.
- Some Big Cannons service classes contain TODO placeholders for deeper CBC API
  ownership/impact integration.
- Aeronautics/Offroad bore protection currently shares `/claimrules create`
  rather than a dedicated config key.

## Docs Maintenance

- Update `README.md` when the public entrypoint or install story changes.
- Update `docs/server-admin.md` for operator-visible behavior changes.
- Update `docs/configuration.md` for config key/default changes.
- Update `docs/architecture.md` for module, service, persistence, or compat
  boundary changes.
- Update `docs/development.md` for build, CI, or workflow changes.
