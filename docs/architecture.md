# Architecture

This repository is a Gradle multi-module NeoForge project with a Minecraft
`1.21.1` compile baseline and experimental metadata for Minecraft
`[1.21.1,1.27)` on NeoForge `[21.1.232,27.0)`. Open Parties and
Claims is the source of party membership and claim
ownership. This project adds territory-aware services, war state, protection
rules, and optional compat behavior.

## Modules

| Gradle project | Mod id | Role |
| --- | --- | --- |
| `customclaims-core` | `customclaims_core` | Shared OPaC adapters, territory services, permissions, storage, messages, logs, and rollback interface. |
| `customclaims-war` | `customclaims_war` | War lifecycle, raid windows, border checks, capture progress, war lives, HUD, notifications, and war commands. |
| `customclaims-protection` | `customclaims_protection` | Claim rules, foreign interaction limits, explosions, storage, Wither rules, villager/trader protection, GUI payloads, and `/claimrules`. |
| `customclaims-create` | `customclaims_create` | Optional Create contraption movement and block-breaking hooks. |
| `customclaims-aeronautics` | `customclaims_aeronautics` | Optional Aeronautics/Offroad bore-mining and Sable assembly hooks. |
| `customclaims-big-cannons` | `customclaims_big_cannons` | Optional Create Big Cannons launch and terrain-damage hooks. |
| `customclaims-xaero` | `customclaims_xaero` | Configurable global/filtered war-marker sync, optional client waypoints/overlay, and Xaero World Map war-start action. |
| `opac-warfare` | multiple mod ids | Single-jar distribution that bundles the seven functional modules. |

## Dependency Shape

Project dependencies:

```text
customclaims-core
  <- customclaims-war
  <- customclaims-protection
       <- customclaims-create
       <- customclaims-aeronautics
       <- customclaims-big-cannons
customclaims-war
  <- customclaims-protection
  <- customclaims-big-cannons
  <- customclaims-xaero
opac-warfare bundles all functional modules
```

External dependencies:

- `customclaims-core` requires Open Parties and Claims.
- `customclaims-create` has optional Create integration guarded by
  `ModList.get().isLoaded("create")`.
- `customclaims-aeronautics` has optional Aeronautics/Offroad integration guarded
  by `ModList.get().isLoaded("offroad")` and optional mixins.
- `customclaims-big-cannons` has optional Create Big Cannons integration guarded
  by `ModList.get().isLoaded("createbigcannons")`.
- `customclaims-xaero` has optional client-side Xaero behavior, including a
  Xaero World Map right-click war-start mixin, global active-war marker
  visibility by default, and configurable side/admin/radius filtering.

## Service Records

The code uses small service containers created by module entrypoints:

- `CoreServices` creates OPaC adapters, party/claim/territory services,
  permission service, data storage, logs, message service, and rollback service.
- `WarServices` builds war storage, side attack/defense cooldown storage, optional daily
  outgoing/accepted start-limit storage, raid windows, border checks, AFK tracking,
  capture progress, display/HUD, notifications, lives, scoreboard, and `WarManager`.
- `ProtectionServices` builds foreign interaction limits, OPaC bypass service,
  explosion protection, Create mining/assembly rules, claim-rule cooldowns,
  `/claimrules` orchestration, storage protection, Wither rules, and
  villager/trader protection.

When adding behavior, prefer placing domain logic in a service and keeping event
handlers and command classes thin.

## Entrypoints And Events

Core:

- `CustomClaimsCoreMod` registers `CoreConfig` and initializes
  `CoreServices`.

War:

- `CustomClaimsWarMod` registers `WarConfig` and initializes `WarServices`.
- Registers `/war` and `/waradmin`.
- Handles server ticks, block/entity interaction activity, and death events for
  war lives.

Protection:

- `CustomClaimsProtectionMod` registers `ProtectionConfig`, claim-rule payloads,
  and client-only GUI/keybind registration when running on the client.
- Registers `/claimrules`.
- Handles block interaction, break/place, explosions, storage interactions,
  Wither spawn, villager/trader damage, and foreign interaction reset ticks.

Compat:

- `CustomClaimsCreateMod` activates Create hooks only when Create is loaded.
- `CustomClaimsAeronauticsMod` activates Offroad bore-mining hooks and optional Sable assembly checks only when their target mods are loaded.
- `CustomClaimsBigCannonsMod` activates CBC hooks only when Create Big Cannons
  is loaded.
- `CustomClaimsXaeroMod` registers marker payloads, the serverbound Xaero map
  war-start payload, server tick marker sync, and client-only waypoint/overlay
  behavior. The map action sends only dimension and chunk; server permission,
  same-dimension, distance, and normal war checks remain authoritative.

## Persistence

All project-owned runtime files are resolved under:

```text
world/customclaims/
```

Current file names:

- `war/active-wars.dat`: non-terminal wars and original claim snapshots.
- `war/side-cooldowns.dat`: fixed attack/defense window timestamps and consumed
  target-chunk slots per side.
- `war/daily-starts.dat`: optional successful current-day war starts per attacking side.
- `war/daily-accepted-starts.dat`: optional successful current-day incoming war starts per defending side.
- `logs/war.log`: war lifecycle/progress log.
- `logs/actions.log`: general action log.
- `protection/explosion-protection.txt`: side explosion rule state.
- `protection/create-machines.txt`: side Create/Offroad mining rule state.
- `protection/create-assemblies.txt`: side Create/Sable assembly rule state.
- `protection/claimrule-toggle-cooldowns.txt`: side toggle cooldown timestamps.

Foreign interaction counters are runtime-only. War lives are serialized as part
of active war data.

## Important Invariants

- OPaC remains the authority for party membership and claim ownership.
- Contested chunks use OPaC's built-in `Server` owner only during active war
  handling; original claim snapshots must be preserved so cancel/fail paths can
  restore ownership.
- Internal ownership mutations use OPaC's administrative `claim` operation, not
  the player `tryToClaim` path. Target ownership plus snapshot sub-config and
  forceload data must still be preserved.
- Contested bypass is limited to attacker and defender sides. Outsiders should
  not gain shared chunk access.
- Xaero marker sync may globally broadcast only active/preparing war metadata
  when configured. It must never expose a global claim-owner map or weaken
  server validation of map war-start requests.
- Optional compat code must stay safe when the target mod is not installed, including Aeronautics/Offroad nested modules.
- Client-only registrations must stay behind `FMLEnvironment.dist == Dist.CLIENT`
  checks.
