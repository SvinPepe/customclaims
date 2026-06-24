# Custom Claims

Custom Claims is a NeoForge 1.21.1 multi-module mod project for server-side nations,
Open Parties and Claims territories, chunk wars, and claim protection.

The project currently assumes Open Parties and Claims is installed. OPC is the source
of party membership and claim ownership; the old scoreboard/local fallback is no longer
part of the runtime path.

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

- `customclaims-core`: shared services, permissions, storage, logs, OPC party and
  claim adapter, party display info, territory state, and contested participant checks.
- `customclaims-war`: chunk war flow, preparation/active phases, raid windows,
  border validation, contested claim ownership, capture progress, bossbar/actionbar,
  participant notifications, and war admin commands.
- `customclaims-protection`: block interaction limits, storage rules, explosion
  rules, Wither rules, villager protection, OPC protection bypass for allowed actions,
  and `/claimrules`.
- `customclaims-xaero`: fair-play war overlay payloads and client overlay. It does
  not write Xaero waypoints or persistent map data.
- `customclaims-create`: placeholder module for Create contraption protection hooks.
- `customclaims-big-cannons`: placeholder module for Create Big Cannons projectile
  and ownership rules.

## War Flow

`/war start` starts a war for the chunk the player is standing in.

The target chunk must:

- be claimed by another OPC party;
- not already be in a non-terminal war;
- be attackable from a border, meaning at least one 4-neighbor chunk is wilderness
  or owned by the attacking party;
- pass the optional diagonal border rule if `allow_diagonal_border_chunks` is enabled;
- have at least one online non-AFK defender.

Each party can be involved in only one non-terminal war at a time. `PREPARING` and
`ACTIVE` both count; `FINISHED`, `FAILED`, and `CANCELLED` do not.

When the war becomes active, the target OPC claim is temporarily assigned to the fake
contested owner:

- UUID: `00000000-0000-0000-0000-00000000cc01`
- Name config label: `CC_Contested`

The original claim owner, sub-config index, and forceload flag are saved in war data.
While active, Custom Claims treats the chunk as shared only for the attacker and
defender parties. Those players can break/place and use the configured war protections
in the contested chunk; outsiders do not get the contested bypass.

On finish:

- attacker capture at `100%` transfers the claim to the attacking party through OPC;
- cancel/fail restores the original claim owner snapshot;
- post-war protection is applied for the configured duration.

Active wars are reloaded after server restart. The module re-marks active chunks as
contested and tries to reassert the fake contested owner if OPC no longer has it.

## Capture Balance

Capture starts at `50%` when the war enters the active phase.

Every second:

- each non-AFK attacker in the contested chunk adds `player_weight_per_second`;
- each non-AFK defender in the contested chunk subtracts the same weight;
- if at least one attacker is present, attackers also get
  `attacker_presence_bonus_per_second`;
- if no attacker and no defender is present, progress decays by
  `empty_chunk_decay_per_second`.

Bossbar and actionbar are updated once per second. Participants also receive chat
notifications for declaration, 60s/10s preparation warnings, active start, capture
milestones, empty-chunk decay, admin skip, and war end.

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
```

`/claimrules explosions enable` enables explosion protection for the player's OPC
party. `disable` allows explosions for that party by syncing OPC explosion exception
options where possible.

## Permissions

Players can satisfy permissions either through player tags or by being at the configured
operator level. Console has admin-level access.

- `customclaims.war.start`
- `customclaims.war.status`
- `customclaims.war.admin`
- `customclaims.limits.reset`
- `customclaims.explosions.status`
- `customclaims.explosions.toggle`
- `customclaims.bypass`
- `customclaims.admin`

The operator threshold is configured by `op_permission_level` in the core common config.

## Common Config

NeoForge writes common config files as `config/<modid>-common.toml`, for example
`customclaims_war-common.toml`.

Core:

- `debug_logging = false`
- `op_permission_level = 2`

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
- `war.contested_owner_name = "CC_Contested"`

Protection:

- `explosions.protect_peaceful_claims = true`
- `explosions.allow_in_war_chunks = true`
- `foreign_interaction.block_break_limit = 0`
- `foreign_interaction.block_place_limit = 0`
- `foreign_interaction.limit_reset_interval_seconds = 3600`
- `storage_rules.allow_open_storage_on_foreign_claims = true`
- `storage_rules.protect_storage_from_breaking_on_peaceful_claims = true`
- `storage_rules.allow_storage_breaking_in_war_chunks = true`
- `storage_rules.protected_storage_blocks = ["minecraft:chest", "minecraft:trapped_chest", "minecraft:barrel", "minecraft:shulker_box"]`
- `wither_rules.*`
- `villager_protection.*`

Xaero overlay:

- `xaero_overlay.visible_radius_chunks = 8`

## Xaero Fair-Play Overlay

The Xaero module sends overlay-only active/preparing war markers to clients. It does
not create persistent Xaero waypoints and does not expose a global claim-owner map.

A player receives a marker only when:

- their party attacks or defends that war;
- they are near the war chunk within `xaero_overlay.visible_radius_chunks`;
- they have `customclaims.war.admin`.

The client keeps received markers with a short TTL and draws blinking marker text.
Attackers see `Attack target`, defenders see `Defend target`.

## Runtime Data

Runtime data is stored under the world folder:

```text
world/customclaims/
```

Important files:

- `wars.txt`: persisted war state, including original claim snapshot for contested chunks.
- `logs/war.log`: war lifecycle and progress log.

Foreign interaction counters are runtime-only and are not persisted. They reset globally
on `foreign_interaction.limit_reset_interval_seconds` or through `/claimrules limits resetall`.

## Build

Compile the main modules:

```powershell
gradle --no-daemon "-Dorg.gradle.workers.max=1" :customclaims-core:compileJava :customclaims-war:compileJava :customclaims-protection:compileJava :customclaims-xaero:compileJava -q
```

Build jars for the modules:

```powershell
gradle --no-daemon "-Dorg.gradle.workers.max=1" :customclaims-core:jar :customclaims-war:jar :customclaims-protection:jar :customclaims-xaero:jar -q
```

Full build:

```powershell
gradle --no-daemon "-Dorg.gradle.workers.max=1" build -q
```

If Gradle has just downloaded plugins or a worker daemon has crashed, run:

```powershell
gradle --stop
```

## Local Manual Test

The local test server is expected at:

```text
dvdcraft-test/
```

After rebuilding jars, copy them into `dvdcraft-test/mods`:

```powershell
Copy-Item customclaims-core\build\libs\customclaims_core-0.1.0.jar dvdcraft-test\mods\customclaims_core-0.1.0.jar -Force
Copy-Item customclaims-war\build\libs\customclaims_war-0.1.0.jar dvdcraft-test\mods\customclaims_war-0.1.0.jar -Force
Copy-Item customclaims-protection\build\libs\customclaims_protection-0.1.0.jar dvdcraft-test\mods\customclaims_protection-0.1.0.jar -Force
Copy-Item customclaims-xaero\build\libs\customclaims_xaero-0.1.0.jar dvdcraft-test\mods\customclaims_xaero-0.1.0.jar -Force
```

Suggested test pass:

1. Start `dvdcraft-test` with OPC `0.27.5`.
2. Create two OPC parties and claim chunks for the defender.
3. Stand in a defender border chunk and run `/war start`.
4. Run `/waradmin skipprep here` for a fast active-phase test.
5. Verify the claim owner becomes the configured contested fake owner.
6. Verify attacker and defender can break/place in the contested chunk.
7. Verify outsiders do not receive contested access.
8. Verify progress starts at `50%`, attacker bonus applies, and empty chunk decay works.
9. Finish capture or cancel the war and verify the claim is transferred/restored.
10. Check `/war list`, `/war near`, bossbar/actionbar, notifications, and Xaero overlay visibility.
