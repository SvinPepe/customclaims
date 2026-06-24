# Custom Claims

Custom Claims is a NeoForge 1.21.1 multi-module mod project for server-side nations, chunk wars, and claim protection built around an adapter layer for Open Parties and Claims.

## Modules

- `customclaims-core`: shared API, party/claim/chunk models, Open Parties and Claims adapter boundary, fallback adapter, permissions, messages, storage, logs, and rollback contracts.
- `customclaims-war`: chunk-war MVP with `/war start`, `/war status`, `/waradmin stop`, `/waradmin setprogress`, preparation/active phases, raid windows, AFK checks, contested chunks, capture progress, and persisted war data.
- `customclaims-protection`: claim protection MVP for explosions, foreign block interactions, storage, Wither rules, villager protection, and `/claimrules`.
- `customclaims-create`: placeholder module for Create drill/saw/deployer/contraption ownership hooks.
- `customclaims-big-cannons`: placeholder module for Create Big Cannons shot ownership, projectile filtering, impact logs, and own-claim cannon rules.
- `customclaims-xaero`: placeholder module for contested chunk markers, waypoints, packets, and optional client overlays.

## Current MVP

The project intentionally does not implement every rule yet. The first pass establishes the foundation:

- Every external claim/party lookup goes through `ClaimAdapter` and `PartyAdapter`.
- `OpenPartiesClaimAdapter` is currently a fallback implementation. It uses scoreboard teams as temporary party ids and a local claim-owner map for transfer fallback.
- War logic lives in services, not commands or event handlers.
- Protection handlers are thin and delegate to service classes.
- Runtime data is stored below `world/customclaims/`.

## Build

The build is based on the official NeoForge 1.21.1 MDK shape:

- Java toolchain: 21
- NeoGradle userdev: `7.1.38`
- NeoForge: `21.1.234`

Build all modules with:

```bash
gradlew build
```

This repository currently does not include a Gradle wrapper jar because the workspace started empty. Generate or copy a wrapper before using `gradlew` on a machine without Gradle installed.

## Commands In This Pass

- `/war start`
- `/war status`
- `/waradmin stop <warId>`
- `/waradmin setprogress <warId> <value>`
- `/claimrules limits me`
- `/claimrules explosions status`
- `/claimrules explosions enable`
- `/claimrules explosions disable`

## Next Steps

- Replace fallback party/claim logic with the real Open Parties and Claims API.
- Add admin force-claim and reset commands for local testing.
- Persist party explosion-protection toggles.
- Add tests for raid windows, storage filtering, border chunks, and war state transitions.
- Decide exact Create, Create Big Cannons, and Xaero integration APIs for NeoForge 1.21.1.
