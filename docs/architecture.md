# Architecture

This repository is a Gradle multi-module NeoForge project for Minecraft
`1.21.1`. Open Parties and Claims is the source of party membership and claim
ownership. This project adds territory-aware services, war state, protection
rules, and optional compat behavior.

## Modules

| Gradle project | Mod id | Role |
| --- | --- | --- |
| `customclaims-core` | `customclaims_core` | Shared OPaC adapters, territory services, permissions, storage, messages, logs, and rollback interface. |
| `customclaims-war` | `customclaims_war` | War lifecycle, raid windows, border checks, capture progress, war lives, HUD, notifications, and war commands. |
| `customclaims-protection` | `customclaims_protection` | Claim rules, foreign interaction limits, explosions, storage, Wither rules, villager/trader protection, GUI payloads, and `/claimrules`. |
| `customclaims-create` | `customclaims_create` | Optional Create contraption movement and block-breaking hooks. |
| `customclaims-aeronautics` | `customclaims_aeronautics` | Optional Aeronautics/Offroad Borehead Bearing and Rock Cutting Wheel mining hooks. |
| `customclaims-big-cannons` | `customclaims_big_cannons` | Optional Create Big Cannons launch and terrain-damage hooks. |
| `customclaims-xaero` | `customclaims_xaero` | Fair-play server marker sync and optional client waypoint/overlay behavior. |
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
- `customclaims-xaero` has optional client-side Xaero behavior and still keeps
  server marker visibility fair-play scoped.

## Service Records

The code uses small service containers created by module entrypoints:

- `CoreServices` creates OPaC adapters, party/claim/territory services,
  permission service, data storage, logs, message service, and rollback service.
- `WarServices` builds war storage, raid windows, border checks, AFK tracking,
  capture progress, post-war protection, display/HUD, notifications, lives,
  scoreboard, and `WarManager`.
- `ProtectionServices` builds foreign interaction limits, OPaC bypass service,
  explosion protection, Create machine rules, claim-rule cooldowns,
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
- `CustomClaimsAeronauticsMod` activates Offroad bore-mining hooks only when Offroad is loaded.
- `CustomClaimsBigCannonsMod` activates CBC hooks only when Create Big Cannons
  is loaded.
- `CustomClaimsXaeroMod` registers marker payloads, server tick marker sync, and
  client-only waypoint/overlay behavior.

## Persistence

All project-owned runtime files are resolved under:

```text
world/customclaims/
```

Current file names:

- `war/active-wars.dat`: non-terminal wars and original claim snapshots.
- `logs/war.log`: war lifecycle/progress log.
- `logs/actions.log`: general action log.
- `protection/explosion-protection.txt`: side explosion rule state.
- `protection/create-machines.txt`: side Create machine rule state.
- `protection/claimrule-toggle-cooldowns.txt`: side toggle cooldown timestamps.

Foreign interaction counters are runtime-only. War lives are serialized as part
of active war data.

## Important Invariants

- OPaC remains the authority for party membership and claim ownership.
- Contested chunks use the configured fake owner only during active war handling;
  original claim snapshots must be preserved so cancel/fail paths can restore
  ownership.
- Contested bypass is limited to attacker and defender sides. Outsiders should
  not gain shared chunk access.
- Xaero marker visibility must remain fair-play scoped. Do not add global claim
  owner map sync.
- Optional compat code must stay safe when the target mod is not installed, including Aeronautics/Offroad nested modules.
- Client-only registrations must stay behind `FMLEnvironment.dist == Dist.CLIENT`
  checks.
