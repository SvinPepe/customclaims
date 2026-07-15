# Server Admin Guide

This guide is for server owners and operators running **Open Parties and
Claims: Warfare** on a NeoForge server.

## Install

Use the `opac-warfare` distribution jar for normal server installs:

```text
opac-warfare/build/libs/opac-warfare-<version>.jar
```

For this release, that file is `opac-warfare-1.6.5.jar`.

Required server mods:

- NeoForge for Minecraft `1.21.1` baseline, with experimental same-jar
  probes allowed through Minecraft `[1.21.1,1.27)` and NeoForge
  `[21.1.232,27.0)`
- Open Parties and Claims `0.27.5` or newer on the baseline stack
- Open Parties and Claims: Warfare

The public jar is one modern artifact. `Minecraft 1.21.1 + NeoForge 21.1.232`
is the full tested baseline; NeoForge `21.x` through `26.x` is experimental
metadata-enabled support and must pass smoke tests before being called verified.
Forge, Fabric, Quilt, older `1.20.x`, and Minecraft `1.27+` are not promised.
See [Compatibility](compatibility.md) before testing adjacent versions.

Optional integrations:

- Create enables independent contraption-assembly and block-breaking protection.
- Create Aeronautics/Offroad enables Borehead Bearing and Rock Cutting Wheel
  protection through `/claimrules create`.
- Sable `2.0.3` enables optional contraption assembly protection through
  `/claimrules assembly`.
- Create Big Cannons enables cannon launch and terrain-damage protection.
- Xaero Minimap or Xaero World Map on clients enables temporary war waypoints
  when the client is compatible.

The `opac-warfare` jar declares multiple mod ids inside one jar. That is
expected: the jar contains core, war, protection, Create compat,
Aeronautics/Offroad compat, Big Cannons compat, and Xaero compat modules.

## First Start Checklist

1. Back up the world.
2. Install Open Parties and Claims and `opac-warfare-*.jar`.
3. Start the server once so NeoForge writes the common config files.
4. Review the generated config files documented in
   [Configuration](configuration.md).
5. Confirm `permissions.default_player_permissions` matches the command access
   you want for normal players.
6. Test `/war status`, `/claimrules explosions status`, `/claimrules create status`,
   and `/claimrules assembly status` with a non-operator player.

## War Flow

`/war start` starts a war for the chunk where the player is standing. The
attacker can be an OPaC party or a solo player without a party.

Compatible Xaero World Map clients can also right-click a map position and
choose `Start war here`. That action is only a request: the server checks
`customclaims.war.start`, `xaero_map_war_start.enabled`, current dimension,
`xaero_map_war_start.max_distance_chunks`, and all normal war target rules
before creating a war.

The target chunk must:

- be claimed by another side;
- not already be in a non-terminal war;
- be attackable from a border, meaning at least one 4-neighbor chunk is
  wilderness or owned by the attacker;
- pass the optional diagonal border rule if
  `allow_diagonal_border_chunks = true`;
- have at least one online non-AFK defender.

Each side can be active in only one role at a time: a side defending any
non-terminal war cannot start an attack, and a side attacking another side
cannot accept an incoming war. `PREPARING` and `ACTIVE` both count.

At the first successful declaration, the attacking side opens a fixed `2` hour
attack window and the defending side opens a fixed `1` hour protection window.
By default each role has one target-chunk slot, so a second outgoing start or
incoming declaration is blocked until its role window expires and its only concurrent-war slot is free. The defender
window protects every claim owned by that side, not merely the originally
targeted chunk.

Each later successful declaration consumes another role slot but does not extend
its window. Raising the corresponding configured cooldown chunk limit to `N`
allows up to `N` parallel wars in that role. The next successful declaration
after a window expires starts a new window even when earlier slots were unused.
Set a cooldown duration to `0` to disable its timed quota; its concurrent-war
chunk limit still applies.

Daily start limits are disabled by default. If configured above `0`, they remain
an additional per-day quota in `raid_window.timezone`; only successful starts
consume it and ending a war does not refund it.

Personal claims are resolved as follows:

- if the claim owner is in an OPaC party, the claim belongs to that party side;
- if the claim owner is not in a party, the claim belongs to that solo player
  side.

## Contested Claims

When a war becomes active, the target OPaC claim is temporarily assigned to a
fake contested owner:

- UUID: `00000000-0000-0000-0000-00000000cc01`
- Name: `Contested War`

The original claim owner, sub-config index, and forceload flag are saved in war
data. While active, the contested chunk is treated as shared only for the
attacker and defender sides. Outsiders do not receive the contested bypass.

On war finish:

- attacker capture at `100%` transfers the claim to the attacker through OPaC;
- `CANCELLED` and `FAILED` endings restore the original claim owner snapshot.

Active wars are reloaded after server restart. The mod re-marks active chunks
as contested and tries to reassert the fake contested owner if OPaC no longer
has it.

## Capture Balance

Capture starts at `war.capture.starting_progress` when the war enters the active
phase. The default is `50.0`.

Every second:

- each non-AFK attacker in the contested chunk adds
  `war.capture.player_weight_per_second`;
- each non-AFK defender in the contested chunk subtracts the same weight;
- players with `0` war lives do not count for capture;
- if at least one attacker is present, attackers also get
  `war.capture.attacker_presence_bonus_per_second`;
- if no attacker and no defender is present, progress decays by
  `war.capture.empty_chunk_decay_per_second`.

Bossbar and actionbar updates run once per second. Participants receive chat
notifications for declarations, preparation warnings, active start, capture
milestones, empty-chunk decay, life loss, admin skip, and war end.

## War Lives

At active start, each current attacker/defender side member gets personal war
lives. The default is `3`.

- A solo side has one member.
- Party members who join after active start have `0` lives for that war.
- Players with `0` lives do not contribute to capture.
- When enabled, active war lives are shown in the vanilla sidebar scoreboard.

## Commands

Player war commands:

```text
/war start
/war status
/war list
/war near
/war near <radius_chunks>
```

`/war near` defaults to radius `8`. The explicit radius accepts `0` through
`32` chunks.

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

Use a resource location for `<dimension>`, for example
`minecraft:overworld`. Progress values accept `0.0` through `100.0`.

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
/claimrules assembly status
/claimrules assembly enable
/claimrules assembly disable
/claimrules gui
```

`/claimrules explosions enable` enables explosion protection for the player's
current side. Players in a party manage nation rules; solo players manage
personal-claim rules.

`/claimrules create disable` blocks Create drills, saws, shared
`BlockBreakingMovementBehaviour` machines, and Aeronautics/Offroad Borehead
Bearing + Rock Cutting Wheel mining on the side's territory. Mining machines are
blocked by default until the side enables them.

`/claimrules assembly disable` independently blocks Create and Sable contraption
assembly. A structure entirely outside claims remains allowed. Once it touches a
claim, all eight corners of its source AABB must be peaceful claims of one side
with assembly enabled; mixed, foreign, unclaimed, and post-war-protected corners
are blocked. Active contested-war chunks retain their existing Create behaviour.

`/claimrules gui` opens the optional client screen when the client also has the
protection module and network channel. Commands remain the full fallback. Client
installs also get an `Open Claim Rules` keybind in the `CustomClaims` controls
category. It defaults to `K` and can be changed through Minecraft
`Settings -> Controls -> CustomClaims`.

Side-level toggles use `claimrules.toggle_cooldown_seconds`. Status commands
and `/claimrules gui` do not spend cooldown. Console and `customclaims.admin`
bypass cooldown.

## Permissions

Players can satisfy permissions through player tags, configured default-player
permissions, or the configured operator level. Console has admin-level access.

Permission nodes:

```text
customclaims.admin
customclaims.bypass
customclaims.war.start
customclaims.war.cancel
customclaims.war.status
customclaims.war.admin
customclaims.rollback
customclaims.limits.reset
customclaims.explosions.status
customclaims.explosions.toggle
customclaims.create.status
customclaims.create.toggle
customclaims.assembly.status
customclaims.assembly.toggle
```

Default player permissions are configured in
`customclaims_core-common.toml`. By default, normal players can start and inspect
wars and can inspect/toggle explosion, Create-mining, and assembly rules. Admin commands and
limit-reset commands are not granted by default.

## Protection Behavior

Explosion protection removes protected claimed blocks from normal explosion
damage when a side has explosion protection enabled. If
`explosions.allow_in_war_chunks = true`, explosion block damage is not filtered
inside contested war chunks.

Storage protection can allow or block opening and breaking protected storage
blocks on foreign peaceful claims. Contested war chunks can allow storage
breaking separately.

Wither and villager/trader protection are controlled by the protection config.
Villager protection has separate contested-war-chunk options so peaceful and war
behavior can differ.

## Xaero Fair-Play Markers

The Xaero module sends active/preparing war markers only to clients allowed by
fair-play visibility rules. It does not expose a global claim-owner map.

A player receives a marker only when:

- their side attacks or defends that war;
- they are near the war chunk within `xaero_overlay.visible_radius_chunks`;
- they have `customclaims.war.admin`.

Compatible clients use markers to create named temporary Xaero war waypoints.
Attackers see attack-oriented names, defenders see defense-oriented names, and
war waypoints are removed when the marker is no longer visible.

Xaero World Map clients also get a `Start war here` entry in the map right-click
menu. It does not expose claim-owner map data; it sends only the clicked
dimension and chunk, then the server returns the normal war start success or
failure message.

## Runtime Data

Runtime data is stored under the world folder:

```text
world/customclaims/
```

Important files:

- `war/active-wars.dat`: persisted non-terminal war state, including original
  claim snapshots for contested chunks.
- `war/side-cooldowns.dat`: first-declaration timestamps and consumed attack/
  defense slots per side.
- `war/daily-starts.dat`: current-day successful war starts per attacking side
  for the optional daily outgoing territory fight limit.
- `war/daily-accepted-starts.dat`: current-day successful incoming war starts
  per defending side for the optional daily accepted-war limit.
- `logs/war.log`: war lifecycle and progress log.
- `logs/actions.log`: general action log.
- `protection/explosion-protection.txt`: side explosion protection toggles.
- `protection/create-machines.txt`: side Create/Offroad mining allow/block toggles.
- `protection/create-assemblies.txt`: side Create/Sable assembly allow/block toggles.
- `protection/claimrule-toggle-cooldowns.txt`: last side toggle timestamps for
  cooldowns.

Foreign interaction counters are runtime-only and are not persisted. They reset
globally on `foreign_interaction.limit_reset_interval_seconds` or through
`/claimrules limits resetall`. War lives are persisted with active war data and
survive server restart.

## Operations

- Back up the world before installing, upgrading, or changing protection-heavy
  configs on production servers.
- Keep Open Parties and Claims installed and healthy; this addon relies on OPaC
  as the authority for parties and claims.
- If players report unexpected war starts, inspect raid-window config, role
  cooldown durations and chunk limits, optional daily limits, AFK settings, and
  border settings first.
- If players report unexpected claim damage, inspect side `/claimrules`
  settings, protection config, and whether the chunk is currently contested.
- Use `/waradmin list` before stopping or modifying a war manually.
