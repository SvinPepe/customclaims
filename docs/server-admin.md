# Server Admin Guide

This guide is for server owners and operators running **Open Parties and
Claims: Warfare** on a NeoForge server.

## Install

Use the `opac-warfare` distribution jar for normal server installs:

```text
opac-warfare/build/libs/opac-warfare-<version>.jar
```

For this release, that file is `opac-warfare-2.0.0.jar`.

Required server mods:

- NeoForge for Minecraft `1.21.1` baseline, with experimental same-jar
  probes allowed through Minecraft `[1.21.1,26.0)` and NeoForge
  `[21.1.232,26.0)`
- Open Parties and Claims `0.27.5` or newer on the baseline stack
- Open Parties and Claims: Warfare

The public jar is one modern artifact. `Minecraft 1.21.1 + NeoForge 21.1.232`
is the full tested baseline; newer 1.21.x releases are experimental
metadata-enabled support and must pass smoke tests before being called verified.
Forge, Fabric, Quilt, older `1.20.x`, and Minecraft `26.x` are not supported by
this jar - 26.x has incompatible APIs; see [Compatibility](compatibility.md).
See [Compatibility](compatibility.md) before testing adjacent versions.

Optional integrations:

- Xaero Minimap or Xaero World Map on clients enables temporary war waypoints
  when the client is compatible.

The `opac-warfare` jar declares multiple mod ids inside one jar. That is
expected: the jar contains core, war, and Xaero compat modules.

## First Start Checklist

1. Back up the world.
2. Install Open Parties and Claims and `opac-warfare-*.jar`.
3. Start the server once so NeoForge writes the common config files.
4. Review the generated config files documented in
   [Configuration](configuration.md).
5. Confirm `permissions.default_player_permissions` matches the command access
   you want for normal players.
6. Test `/war status` and `/war start` with a non-operator player.

## War Flow

`/war start` starts a war for the chunk where the player is standing. The
target must be a valid border claim chunk:

- the chunk is claimed by another side;
- the chunk is adjacent to wilderness or your own side's claim (touching by edge
  or diagonal, depending on `allow_diagonal_border_chunks`);
- the target claim has not been protected by a recently finished war or active
  defense window;
- neither your side nor the defending side is currently blocked by active
  cooldown windows or daily war limits;
- the server is not currently in a blocked raid window (`raid_window.*`).

If all checks pass:

1. The target chunk enters `PREPARING` status for `preparation_seconds` (default
   5 minutes). Both sides receive title and chat notifications.
2. During preparation, both sides assemble players. No claim blocks are lost yet.
3. When the preparation timer expires, the war transitions to `ACTIVE`.
4. The sector expands up to `war.sector.max_chunks` connected chunks inwards, up
   to `war.sector.max_conquest_depth_chunks` from wilderness/borders.
5. In the active phase:
   - Contested chunks temporarily switch to server ownership so both attacker
     and defender can interact and engage.
   - Attackers advance capture progress by staying in the contested sector.
   - Defenders push capture progress down by contesting the sector.
   - Anti-zerg capture caps prevent single-side rush scaling beyond configured rates.
   - Participants receive war lives (`war.lives.starting_lives`). Deaths reduce
     lives; when all lives on a side expire, that side loses.
6. If attackers reach `100.0%` capture progress, attackers win: the sector
   chunks are unshared/unclaimed or transferred according to server configuration.
7. If defenders push progress to `0.0%` or the war timer expires, defenders
   repel the attack and claims are restored from snapshots.

## Commands

Player war commands:

```text
/war start
/war status
/war list
/war near
/war near <radius_chunks>
```

- `/war start`: Declares war on the border claim chunk where the player is standing.
- `/war status`: Displays status and capture progress of wars involving the player or party.
- `/war list`: Lists active and preparing wars visible to the player.
- `/war near [radius_chunks]`: Searches for active wars within the given chunk radius (default: 8, max: 32).

Compatible Xaero World Map clients can also right-click any claim chunk within reach and choose `Start war here`. The server authoritatively validates distance, dimension, permissions, and war target rules.

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

- `/waradmin list`: Lists all active and preparing wars on the server with coordinates, status, and capture progress.
- `/waradmin stop here`: Terminates the war in the executor's current chunk and restores claims from snapshots.
- `/waradmin stop chunk <dimension> <chunkX> <chunkZ>`: Terminates the war at the specified coordinates and restores claims.
- `/waradmin skipprep here`: Skips the preparation timer for the war in the executor's current chunk, activating it immediately.
- `/waradmin skipprep chunk <dimension> <chunkX> <chunkZ>`: Skips the preparation timer at the specified coordinates.
- `/waradmin setprogress here <value>`: Sets capture progress (`0.0` to `100.0`) for the war in the executor's current chunk.
- `/waradmin setprogress chunk <dimension> <chunkX> <chunkZ> <value>`: Sets capture progress at the specified coordinates.

> **Note on `/claimrules`**: Claim protection rules and `/claimrules` (explosions, Create contraptions, CBC cannons) are not part of `opac-warfare`. They are provided by the companion mod **[Open Parties and Claims: Better Protection](https://github.com/SvinPepe/opac-better-protection)**.

## Permissions

Permissions can be configured using LuckPerms, NeoForge's built-in `PermissionAPI`, player tags, or the server operator level (`op_permission_level`, default: `2`). Console automatically has admin permissions.

Permission nodes:

| Permission Node | Default | Description |
| --- | --- | --- |
| `opac.warfare.start` | `true` (all players) | Start chunk wars via `/war start` or Xaero World Map. |
| `opac.warfare.status` | `true` (all players) | View war status via `/war status`, `/war list`, and `/war near`. |
| `opac.warfare.cancel` | `false` (OP 2+) | Reserved war cancellation permission. |
| `opac.warfare.admin` | `false` (OP 2+) | Administrative access to all `/waradmin` commands. |
| `opac.warfare.bypass` | `false` (OP 2+) | Bypass war limits, cooldown windows, and raid window restrictions. |
| `opac.warfare.rollback` | `false` (OP 2+) | Perform territory rollback operations. |
| `opac.admin` | `false` (OP 2+) | Global administrative access. |
| `opac.bypass` | `false` (OP 2+) | Global bypass for all checks. |

Legacy aliases (`customclaims.war.start`, `customclaims.war.status`, `customclaims.war.admin`, `customclaims.war.cancel`, `customclaims.war.bypass`, `customclaims.rollback`, `customclaims.admin`, `customclaims.bypass`) are supported for backward compatibility.

Default player permissions are configured in `customclaims_core-common.toml` under `permissions.default_player_permissions`. By default, normal players can start and inspect wars. Admin and bypass permissions are never granted by default.

## Xaero War Markers

By default, `xaero_overlay.visible_to_all_players = true` sends every
active/preparing war marker to every compatible player. This shares only active
war metadata and does not expose a global claim-owner map.

Set the option to `false` to restore filtered visibility. A player then receives
a marker only when:

- their side attacks or defends that war;
- they are near the war chunk within `xaero_overlay.visible_radius_chunks`;
- they have `opac.warfare.admin` (or legacy `customclaims.war.admin`).

Compatible clients use markers to create named temporary Xaero war waypoints.
Attackers see attack-oriented names, defenders see defense-oriented names, and
global observers see neutral war names. Waypoints are removed when the war ends
or the marker is no longer visible.

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
- `metrics/server-id.txt`: random anonymous id used by dedicated-server metrics.

War lives are persisted with active war data and survive server restart.

## Operations

- Back up the world before installing, upgrading, or changing configs on
  production servers.
- Keep Open Parties and Claims installed and healthy; this addon relies on OPaC
  as the authority for parties and claims.
- If players report unexpected war starts, inspect raid-window config, role
  cooldown durations and chunk limits, optional daily limits, AFK settings, and
  border settings first.
- Use `/waradmin list` before stopping or modifying a war manually.
- Set `metrics.enabled = false` in `customclaims_core-common.toml` and restart
  to disable anonymous metrics. An empty `metrics.endpoint` also prevents
  requests.
