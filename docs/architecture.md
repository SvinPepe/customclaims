# Architecture

This repository is a Gradle multi-version and multi-loader project built with **Stonecutter** (`0.9.8`) and **Architectury Loom** (`1.17.493`). Open Parties and Claims is the source of party membership and claim ownership. This project adds territory-aware services, chunk wars, contested claims, raid windows, capture progress, war lives, fair-play Xaero map integration, and discord webhooks across **Minecraft 1.20.1, 1.21.1, and 26.1** on **Forge, NeoForge, and Fabric**.

## Separation of Concerns: Warfare vs. Protection

Starting with version 2.0, claim protection rules and mechanics (Create contraption assembly, drills, saws, CBC/Clockwork cannon projectile impacts, explosion protection, and `/claimrules`) have been cleanly extracted into the companion mod:
👉 **[Open Parties and Claims: Better Protection](https://github.com/SvinPepe/opac-better-protection)**

- **Open Parties and Claims: Warfare** focuses solely on warfare mechanics: declarations, contested chunks, raid windows, conquest sectors, capture timers, war lives, and map sync.
- Both mods communicate through the lightweight `WarfareBridge` API provided by `common` under `dev.customclaims.api.war`.

## System Architecture Diagram

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        opac-warfare (Distribution Jars: Forge, NeoForge, Fabric)       │
│                                                                                        │
│  ┌──────────────────────────────┐              ┌────────────────────────────────────┐  │
│  │         War Domain           │              │            Xaero Domain            │  │
│  │ - War lifecycle & states     │              │ - Fair-play marker sync            │  │
│  │ - Conquest wedges & capture  │              │ - Temporary waypoints              │  │
│  │ - Raid windows & lives       │              │ - Xaero World Map war-start action │  │
│  │ - /war & /waradmin commands  │              └─────────────────┬──────────────────┘  │
│  └──────────────┬───────────────┘                                │                     │
│                 │ registers war status                           │                     │
│                 ▼                                                ▼                     │
│  ┌──────────────────────────────────────────────────────────────────────────────────┐  │
│  │                                 Core Domain                                      │  │
│  │ - OPaC claim adapters & party side identity                                      │  │
│  │ - Common permissions & storage management                                        │  │
│  │ - WarfareBridge API: IWarfareBridge & WarfareBridgeHolder                        │  │
│  │ - Platform Abstractions: IPlatformHelper, IPermissionHelper, INetworkHelper      │  │
│  └───────────────────────┬──────────────────────────────────▲───────────────────────┘  │
└──────────────────────────┼──────────────────────────────────┼──────────────────────────┘
                           │                                  │
                           ▼ queries membership/claims        │ queries contested status
          ┌─────────────────────────────────┐        ┌────────┴─────────────────────────┐
          │     Open Parties and Claims     │        │     opac-better-protection       │
          │    (Authoritative Claims &      │        │  (Companion Mod: Create, CBC,    │
          │         Parties Mod)            │        │   explosions, /claimrules)       │
          └─────────────────────────────────┘        └──────────────────────────────────┘
```

## Modules

| Gradle project | Target Loaders | Target Versions | Role |
| --- | --- | --- | --- |
| `common` | Shared / All | `1.20.1`, `1.21.1`, `26.1` | Shared domain logic: Core services, OPaC adapters, War services, Xaero marker sync, and the public `WarfareBridge` provider API (`IWarfareBridge`, `WarfareBridgeHolder`). |
| `neoforge` | NeoForge | `1.21.1`, `26.1` | NeoForge platform implementations (`NeoForgePlatformHelper`, `NeoForgePermissionHelper`, `NeoForgeNetworkHelper`, `OpacWarfareMod`). |
| `fabric` | Fabric | `1.20.1`, `1.21.1` | Fabric platform implementations (`FabricPlatformHelper`, `FabricPermissionHelper`, `FabricNetworkHelper`, `CustomClaimsFabricMod`, `CustomClaimsFabricClientMod`). |
| `forge` | Forge | `1.20.1` | Minecraft Forge platform implementations (`ForgePlatformHelper`, `ForgePermissionHelper`, `ForgeNetworkHelper`, `OpacWarfareForgeMod`). |

## Distribution Jars

Each loader and version branch outputs a dedicated distribution jar:
- `forge/versions/1.20.1/build/libs/opac-warfare-1.20.1-forge.jar`
- `fabric/versions/1.20.1/build/libs/opac-warfare-1.20.1-fabric.jar`
- `neoforge/versions/1.21.1/build/libs/opac-warfare-1.21.1-neoforge.jar`
- `fabric/versions/1.21.1/build/libs/opac-warfare-1.21.1-fabric.jar`
- `neoforge/versions/26.1/build/libs/opac-warfare-26.1-neoforge.jar`

## WarfareBridge API (External Integration)

The `WarfareBridge` API allows external mods (such as `Open Parties and Claims: Better Protection`) to check whether a specific chunk is currently contested in an active war, without requiring a compile-time or runtime hard dependency on war domain logic.

### API Classes

Both classes reside in `common` under `dev.customclaims.api.war`:

1. **`IWarfareBridge`**:
   ```java
   package dev.customclaims.api.war;

   import net.minecraft.server.level.ServerLevel;
   import net.minecraft.world.level.ChunkPos;

   public interface IWarfareBridge {
       boolean isContestedWarChunk(ServerLevel level, ChunkPos chunkPos);
   }
   ```

2. **`WarfareBridgeHolder`**:
   ```java
   package dev.customclaims.api.war;

   public final class WarfareBridgeHolder {
       public static void register(IWarfareBridge newBridge);
       public static boolean isContestedWarChunk(ServerLevel level, ChunkPos chunkPos);
   }
   ```

### Lifecycle & Mechanics

- **Provider Registration**: During initialization, `WarManager` registers its contested chunk lookup implementation into `WarfareBridgeHolder`:
  ```java
  WarfareBridgeHolder.register(this::isContestedWarChunk);
  ```
- **Consumer Querying**: Companion mods (like `opac-better-protection`) invoke:
  ```java
  boolean inWar = WarfareBridgeHolder.isContestedWarChunk(level, chunkPos);
  ```
  If `inWar` is `true`, protection handlers allow explosions, block modification by contraptions, and projectile impact damage inside the war zone. If `false`, standard OPaC claim protections apply.
- **Fail-Safe Fallback**: If `customclaims-war` is not loaded or no bridge is registered, `WarfareBridgeHolder` safely returns `false` without throwing exceptions.

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
- `WarfareBridgeHolder` must maintain backwards compatibility and null-safe defaults for companion mods.
- Xaero marker sync may globally broadcast only active/preparing war metadata when configured. It must never expose a global claim-owner map or weaken server validation of map war-start requests.
- Client-only registrations must stay behind client-side checks.

