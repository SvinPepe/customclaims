# Architecture

This repository is a Gradle multi-module project with a Minecraft `1.21.1` compile baseline and experimental metadata for Minecraft `[1.21.1,26.0)` on NeoForge `[21.1.232,26.0)`. Open Parties and Claims is the source of party membership and claim ownership. This project adds territory-aware services, chunk wars, contested claims, raid windows, capture progress, war lives, fair-play Xaero map integration, and discord webhooks.

For details on the forward-looking multi-loader (Fabric + NeoForge via Architectury) and multi-version (Minecraft 1.21.1 and 26.1 via Stonecutter) plan, see [Multi-Loader & Multi-Version Architecture](multiversion-multiloader.md).

## Modules

| Gradle project | Mod id | Role |
| --- | --- | --- |
| `customclaims-core` | `customclaims_core` | Shared OPaC adapters, territory services, permissions, storage, messages, logs, anonymous metrics, and rollback interface. |
| `customclaims-war` | `customclaims_war` | War lifecycle, raid windows, border checks, capture progress, war lives, HUD, notifications, Discord webhooks, and war commands. |
| `customclaims-xaero` | `customclaims_xaero` | Fair-play war-marker sync, client waypoints/overlay, and Xaero World Map war-start action. |
| `opac-warfare` | multiple mod ids | Single-jar distribution that bundles `customclaims-core`, `customclaims-war`, and `customclaims-xaero`. |

## Dependency Shape

Project dependencies:

```text
customclaims-core
  <- customclaims-war
  <- customclaims-xaero
opac-warfare bundles customclaims-core, customclaims-war, customclaims-xaero
```

External dependencies:

- `customclaims-core` requires Open Parties and Claims.
- `customclaims-xaero` has optional client-side Xaero behavior, including a Xaero World Map right-click war-start mixin, global active-war marker visibility by default, and configurable side/admin/radius filtering.

## Service Records

The code uses small service containers created by module entrypoints:

- `CoreServices` creates OPaC adapters, party/claim/territory services, permission service, data storage, logs, message service, anonymous dedicated-server metrics, and rollback service.
- `WarServices` builds war storage, side attack/defense cooldown storage, optional daily outgoing/accepted start-limit storage, raid windows, border checks, AFK tracking, capture progress, display/HUD, notifications, lives, scoreboard, and `WarManager`.

When adding behavior, prefer placing domain logic in a service and keeping event handlers and command classes thin.

## Entrypoints And Events

Core:

- `CustomClaimsCoreMod` registers `CoreConfig` and initializes `CoreServices`. Its metrics service listens for server start, tick, and stop events; HTTP delivery runs asynchronously off the server thread.

War:

- `CustomClaimsWarMod` registers `WarConfig` and initializes `WarServices`.
- Registers `/war` and `/waradmin`.
- Handles server ticks, block/entity interaction activity, and death events for war lives.

Compat:

- `CustomClaimsXaeroMod` registers marker payloads, the serverbound Xaero map war-start payload, server tick marker sync, and client-only waypoint/overlay behavior. The map action sends only dimension and chunk; server permission, same-dimension, distance, and normal war checks remain authoritative.

## Persistence

All project-owned runtime files are resolved under:

```text
world/customclaims/
```

Current file names:

- `war/active-wars.dat`: non-terminal wars and original claim snapshots.
- `war/side-cooldowns.dat`: fixed attack/defense window timestamps and consumed target-chunk slots per side.
- `war/daily-starts.dat`: optional successful current-day war starts per attacking side.
- `war/daily-accepted-starts.dat`: optional successful current-day incoming war starts per defending side.
- `logs/war.log`: war lifecycle/progress log.
- `logs/actions.log`: general action log.
- `metrics/server-id.txt`: randomly generated anonymous dedicated-server id.

War lives are serialized as part of active war data.

## Important Invariants

- OPaC remains the authority for party membership and claim ownership.
- Contested chunks use OPaC's built-in `Server` owner only during active war handling; original claim snapshots must be preserved so cancel/fail paths can restore ownership.
- Internal ownership mutations use OPaC's administrative `claim` operation, not the player `tryToClaim` path. Target ownership plus snapshot sub-config and forceload data must still be preserved.
- Contested bypass is limited to attacker and defender sides. Outsiders should not gain shared chunk access.
- Xaero marker sync may globally broadcast only active/preparing war metadata when configured. It must never expose a global claim-owner map or weaken server validation of map war-start requests.
- Client-only registrations must stay behind client-side checks.
