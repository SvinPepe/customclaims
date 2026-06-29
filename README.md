# Open Parties and Claims: Warfare

**Open Parties and Claims: Warfare** is an unofficial NeoForge server-side addon for Open Parties and Claims.

It adds chunk wars, contested claims, configurable protection rules, Create machine control, Create Big Cannons protection, and optional Xaero war waypoints.

Open Parties and Claims remains the source of party membership and claim ownership. This addon builds warfare and protection mechanics on top of OPaC territories.

## Requirements

- Minecraft `1.21.1`
- NeoForge `21.1.232` or newer in the configured range
- Java toolchain `21`
- Open Parties and Claims `neoforge-1.21.1-0.27.5` or newer

The Gradle dependency is resolved from Modrinth Maven:

```gradle
maven.modrinth:open-parties-and-claims:${opc_version}
```

## Modules

- `customclaims-core`: shared services, permissions, storage, logs, OPC party/personal
  claim adapter, side display info, territory state, and contested participant checks.
- `customclaims-war`: chunk war flow, preparation/active phases, raid windows,
  border validation, contested claim ownership, capture progress, bossbar/actionbar,
  participant notifications, and war admin commands.
- `customclaims-protection`: block interaction limits, storage rules, Open Parties and Claims: Warfare
  explosion filtering with best-effort OPC sync, Wither rules, villager protection,
  OPC protection bypass for allowed actions, side claimrules cooldowns, the
  CustomClaims client rules GUI, and `/claimrules`.
- `customclaims-xaero`: fair-play war marker payloads, Xaero temporary war
  waypoints, and an optional legacy client overlay. It does not send global
  claim-owner map data.
- `customclaims-create`: optional Create integration for claimrules-controlled
  contraption block-breaking and claimed-block movement protection.
- `customclaims-big-cannons`: optional Create Big Cannons integration for terrain
  damage filtering and projectile launch blocking from protected claims.

## War Flow

`/war start` starts a war for the chunk the player is standing in. The attacker can
be an OPC party or a solo player without a party.

The target chunk must:

- be claimed by another side;
- not already be in a non-terminal war;
- be attackable from a border, meaning at least one 4-neighbor chunk is wilderness
  or owned by the attacking side;
- pass the optional diagonal border rule if `allow_diagonal_border_chunks` is enabled;
- have at least one online non-AFK defender.

Each side can be involved in only one non-terminal war at a time by default.
`PREPARING` and `ACTIVE` both count; `FINISHED`, `FAILED`, and `CANCELLED` do not.

Personal claims are treated as follows:

- if the claim owner is in an OPC party, the claim belongs to that party side;
- if the claim owner is not in a party, the claim belongs to that solo player side.

When the war becomes active, the target OPC claim is temporarily assigned to the fake
contested owner:

- UUID: `00000000-0000-0000-0000-00000000cc01`
- Name config label: `Contested War`

The original claim owner, sub-config index, and forceload flag are saved in war data.
While active, Custom Claims treats the chunk as shared only for the attacker and
defender sides. Those players can break/place and use the configured war protections
in the contested chunk; outsiders do not get the contested bypass.

At active start, every current attacker/defender side member gets personal war lives.
The default is `3`. A solo side has one member. New party members who join after
active start have `0` lives for that war.

On finish:

- attacker capture at `100%` transfers the claim to the attacking side through OPC;
- cancel/fail restores the original claim owner snapshot;
- post-war protection is applied for the configured duration.

Active wars are reloaded after server restart. The module re-marks active chunks as
contested and tries to reassert the fake contested owner if OPC no longer has it.

## Capture Balance

Capture starts at `50%` when the war enters the active phase.

Every second:

- each non-AFK attacker in the contested chunk adds `player_weight_per_second`;
- each non-AFK defender in the contested chunk subtracts the same weight;
- players with `0` war lives do not count as attackers or defenders for capture;
- if at least one attacker is present, attackers also get
  `attacker_presence_bonus_per_second`;
- if no attacker and no defender is present, progress decays by
  `empty_chunk_decay_per_second`.

Bossbar and actionbar are updated once per second. Participants also receive chat
notifications for declaration, 60s/10s preparation warnings, active start, capture
milestones, empty-chunk decay, life loss, admin skip, and war end. Active war lives
are shown in the vanilla sidebar scoreboard when enabled.

## Commands

War player commands:

```text
/war start
/war status
/war list
/war near
/war near <radius_chunks>
```

Admin war commands:

```text
/waradmin list
/waradmin stop here
/waradmin stop chunk <dimension> <chunkX> <chunkZ>
/waradmin skipprep here
/waradmin skipprep chunk <dimension> <chunkX> <chunkZ>
/waradmin setprogress here <value>
/waradmin setprogress chunk <dimension> <chunkX> <chunkZ> <value>
```

Use a resource location for `<dimension>`, for example `minecraft:overworld`.

Protection commands:

```text
/claimrules limits me
/claimrules limits reset <player>
/claimrules limits resetall
/claimrules explosions status
/claimrules explosions enable
/claimrules explosions disable
/claimrules create status
/claimrules create enable
/claimrules create disable
/claimrules gui
```

`/claimrules explosions enable` enables Open Parties and Claims: Warfare explosion protection for the
player's current side. Players in a party manage nation rules; players without a
party manage personal-claim rules. The setting is stored by Open Parties and Claims: Warfare and synced
to OPC explosion exception options where possible.

`/claimrules create disable` blocks Create drills, saws, other shared
`BlockBreakingMovementBehaviour` machines, and contraption movement of claimed blocks
on the side's territory. `enable` allows those Create machines on that side's
territory. Create machines are blocked by default until the side enables them.

`/claimrules gui` opens the optional CustomClaims client screen with the explosion
and Create toggles. If the client module is not installed, the commands remain the
full fallback. Client installs also get an `Open Claim Rules` keybind in the
`CustomClaims` controls category, defaulting to `K`. Cooldown timers tick down live
in the GUI and disabled toggle buttons show the remaining time.

Side-level toggles have a cooldown from `claimrules.toggle_cooldown_seconds`
(default `600`). Status commands and `/claimrules gui` do not spend cooldown.
Console and `customclaims.admin` bypass the cooldown.

When enabled, Open Parties and Claims: Warfare removes protected claimed blocks from normal explosion
damage and blocks Create Big Cannons terrain damage in protected chunks. With the
Big Cannons compat module loaded, mounted big cannon shots and drop mortar shots
from protected claimed chunks are cancelled before CBC consumes the loaded munition.
The old projectile spawn-cancel path remains as a fallback for unsupported CBC launch
paths.

## Permissions

Players can satisfy permissions through player tags, configured default-player
permissions, or the configured operator level. Console has admin-level access.

- `customclaims.war.start`
- `customclaims.war.status`
- `customclaims.war.admin`
- `customclaims.limits.reset`
- `customclaims.explosions.status`
- `customclaims.explosions.toggle`
- `customclaims.create.status`
- `customclaims.create.toggle`
- `customclaims.bypass`
- `customclaims.admin`

The operator threshold is configured by `op_permission_level` in the core common config.
By default, ordinary players can use `/war start`, `/war status/list/near`, and
`/claimrules explosions|create status/enable/disable`; admin commands and limits
reset commands are not granted by default.

## Common Config

NeoForge writes common config files as `config/<modid>-common.toml`, for example
`customclaims_war-common.toml`.

Core:

- `debug_logging = false`
- `op_permission_level = 2`
- `permissions.default_player_permissions = ["customclaims.war.start", "customclaims.war.status", "customclaims.explosions.status", "customclaims.explosions.toggle", "customclaims.create.status", "customclaims.create.toggle"]`

War:

- `preparation_seconds = 300`
- `max_duration_seconds = 3600`
- `war.capture.starting_progress = 50.0`
- `war.capture.player_weight_per_second = 0.35`
- `war.capture.attacker_presence_bonus_per_second = 0.25`
- `war.capture.empty_chunk_decay_per_second = 0.50`
- `max_active_wars_per_party = 1`
- `allow_diagonal_border_chunks = false`
- `afk_seconds = 300`
- `raid_window.enable_raid_window = true`
- `raid_window.timezone = "Europe/Moscow"`
- `raid_window.blocked_windows = ["04:00-08:00"]`
- `raid_window.allow_ongoing_wars_to_continue_after_window_start = true`
- `post_war_protection_seconds = 1800`
- `war_ui.bossbar_visible_radius_chunks = 3`
- `war.contested_owner_uuid = "00000000-0000-0000-0000-00000000cc01"`
- `war.contested_owner_name = "Contested War"`
- `war.lives.starting_lives = 3`
- `war.lives.scoreboard_sidebar_enabled = true`
- `war.lives.scoreboard_objective = "cc_war_lives"`

Protection:

- `foreign_interaction.block_break_limit = 0`
- `foreign_interaction.block_place_limit = 0`
- `foreign_interaction.limit_reset_interval_seconds = 3600`
- `claimrules.toggle_cooldown_seconds = 600`
- `explosions.custom_filter_enabled = true`
- `explosions.allow_in_war_chunks = true`
- `big_cannons.block_projectile_launch_from_protected_claims = true`
- `big_cannons.log_blocked_projectiles = true`
- `storage_rules.allow_open_storage_on_foreign_claims = true`
- `storage_rules.protect_storage_from_breaking_on_peaceful_claims = true`
- `storage_rules.allow_storage_breaking_in_war_chunks = true`
- `storage_rules.protected_storage_blocks = ["minecraft:chest", "minecraft:trapped_chest", "minecraft:barrel", "minecraft:shulker_box"]`
- `wither_rules.*`
- `villager_protection.*`

Xaero:

- `xaero_overlay.visible_radius_chunks = 8`
- `xaero_overlay.custom_overlay_enabled = false`
- `xaero_waypoints.enabled = true`
- `xaero_waypoints.refresh_interval_seconds = 5`

## Xaero Fair-Play Markers

The Xaero module sends active/preparing war markers only to clients allowed by the
fair-play visibility rules. Compatible clients use those markers to create temporary
Xaero war waypoints. The old Open Parties and Claims: Warfare HUD overlay is still present for future
development, but it is disabled by default through `xaero_overlay.custom_overlay_enabled`.

The module does not expose a global claim-owner map.

A player receives a marker only when:

- their side attacks or defends that war;
- they are near the war chunk within `xaero_overlay.visible_radius_chunks`;
- they have `customclaims.war.admin`.

The client refreshes temporary waypoints at most once per
`xaero_waypoints.refresh_interval_seconds`. Attackers see `Attack: A vs B`, defenders
see `Defend: A vs B` in named waypoints when the installed Xaero API supports it;
otherwise Open Parties and Claims: Warfare falls back to the existing temporary coordinate waypoint.

## Runtime Data

Runtime data is stored under the world folder:

```text
world/customclaims/
```

Important files:

- `wars.txt`: persisted war state, including original claim snapshot for contested chunks.
- `logs/war.log`: war lifecycle and progress log.
- `protection/explosion-protection.txt`: Open Parties and Claims: Warfare side explosion protection toggles.
- `protection/create-machines.txt`: side Create-machine allow/block toggles.
- `protection/claimrule-toggle-cooldowns.txt`: last side toggle timestamps for cooldowns.

Foreign interaction counters are runtime-only and are not persisted. They reset globally
on `foreign_interaction.limit_reset_interval_seconds` or through `/claimrules limits resetall`.
War lives are persisted with active war data and survive server restart.

## Build

Compile the main modules:

```powershell
gradle --no-daemon "-Dorg.gradle.workers.max=1" :customclaims-core:compileJava :customclaims-war:compileJava :customclaims-protection:compileJava :customclaims-create:compileJava :customclaims-big-cannons:compileJava :customclaims-xaero:compileJava -q
```

Build jars for the modules:

```powershell
gradle --no-daemon "-Dorg.gradle.workers.max=1" :customclaims-core:jar :customclaims-war:jar :customclaims-protection:jar :customclaims-create:jar :customclaims-big-cannons:jar :customclaims-xaero:jar -q
```

Full build:

```powershell
gradle --no-daemon "-Dorg.gradle.workers.max=1" build -q
```

If Gradle has just downloaded plugins or a worker daemon has crashed, run:

```powershell
gradle --stop
```
