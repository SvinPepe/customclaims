# Open Parties and Claims: Warfare

**Open Parties and Claims: Warfare** is an unofficial NeoForge addon for
[Open Parties and Claims](https://modrinth.com/mod/open-parties-and-claims).

[GitHub](https://github.com/SvinPepe/opac-warfare) |
[Modrinth](https://modrinth.com/mod/open-parties-and-claims-warfare) |
[Anonymous metrics](https://customclaims-metrics.svinpepe.workers.dev/)

It adds chunk wars, contested claims, side-based attack and defense cooldowns with configurable target-chunk slots,
raid windows, capture progress, war lives, Discord webhooks, and fair-play Xaero minimap/worldmap marker synchronization.
Open Parties and Claims remains the source of party membership and claim ownership; this project builds warfare mechanics
on top of OPaC territories.

## Separation of Concerns: Warfare vs. Protection

Starting with Warfare 2.0, claim protection rules and mechanics (Create contraption assembly, drills, saws, CBC/Clockwork cannon projectile impacts, explosion protection, and `/claimrules`) have been separated into a dedicated companion mod:

👉 **[Open Parties and Claims: Better Protection](https://github.com/SvinPepe/opac-better-protection)**

- **Open Parties and Claims: Warfare** (this mod) focuses strictly on territorial conquest, contested claims, raid windows, capture progress, war lives, fair-play Xaero waypoint sync, and Discord webhooks.
- When used alongside **Better Protection**, the two mods integrate seamlessly via `WarfareBridge` (`dev.customclaims.api.war.WarfareBridgeHolder`): `opac-better-protection` queries active war status to permit siege damage and contraption breaches inside contested war chunks while preserving claim safety elsewhere.

## Multi-Loader and Multi-Version Status

- **Tested Baseline**: Minecraft `1.21.1` on NeoForge `21.1.232` (Java `21`). Jar metadata allows experimental same-jar probes on `[1.21.1,26.0)` and NeoForge `[21.1.232,26.0)`.
- **Architectural Readiness**: The project architecture is structured for multi-loader support (Fabric + NeoForge via Architectury Loom abstractions for platform, networking, and permissions) and multi-version builds (Minecraft `1.21.1` and `26.1` via Stonecutter). For detailed architecture specifications, see [Multi-Loader & Multi-Version Architecture](docs/multiversion-multiloader.md).

## Requirements

- Minecraft `1.21.1` baseline; metadata allows experimental `[1.21.1,26.0)` probes (supports 1.21.x through 1.21.11)
- NeoForge `21.1.232` baseline; metadata allows experimental `[21.1.232,26.0)` probes
- Java `21`
- Open Parties and Claims `neoforge-1.21.1-0.27.5` or newer on the baseline stack

Optional integrations activate only when their target mods are installed:

- Xaero Minimap or Xaero World Map on compatible clients; Xaero World Map
  also enables a right-click map war-start action

## What To Install

Server owners install the single distribution jar built by the `opac-warfare` module:

```text
opac-warfare/build/libs/opac-warfare-<version>.jar
```

This single distribution jar bundles:
- `customclaims_core`
- `customclaims_war`
- `customclaims_xaero`

## Quick Start

Build the artifact:

```powershell
.\gradlew.bat --no-daemon :opac-warfare:jar test
```

On Linux/macOS:

```sh
./gradlew --no-daemon :opac-warfare:jar test
```

Copy the resulting `opac-warfare-*.jar` into the server `mods/` folder together
with Open Parties and Claims.

After first server start, NeoForge writes common config files under `config/`.
See [Configuration](docs/configuration.md) before using the mod on a production
world.

## Anonymous Metrics

On dedicated servers, CustomClaims can send an anonymous heartbeat every 30
minutes to the HTTP(S) endpoint configured as `metrics.endpoint` in
`config/customclaims_core-common.toml`. The default endpoint is
`https://customclaims-metrics.svinpepe.workers.dev/heartbeat`.

Each heartbeat contains only:

- a randomly generated `server_id`, stored locally in
  `world/customclaims/metrics/server-id.txt`;
- the CustomClaims, Minecraft, and NeoForge versions;
- the current number of online players;
- boolean presence flags for OPaC, Xaero, and Corpse.

It does not include the server IP or hostname, player names, the world seed, or
other identifying data. Metrics never run in an integrated/client server. To
disable them, set `metrics.enabled = false` or set `metrics.endpoint = ""` in
`config/customclaims_core-common.toml` and restart the server.

## Main Commands

Player war commands:

```text
/war start
/war status
/war list
/war near
/war near <radius_chunks>
```

Compatible Xaero World Map clients can also right-click the map and choose
`Start war here`. The server still enforces war-start permission, same-dimension
and distance limits, side cooldowns, optional daily quotas, and all normal war target rules.

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

## Documentation

- [Server Admin Guide](docs/server-admin.md): installation, gameplay flow,
  commands, permissions, runtime data, and operational notes.
- [Compatibility](docs/compatibility.md): supported Minecraft/NeoForge target,
  one-jar policy, and adjacent-version probe workflow.
- [Configuration](docs/configuration.md): all common config files and keys.
- [Architecture](docs/architecture.md): modules, services, event entrypoints,
  persistence, and compat boundaries.
- [Multi-Loader & Multi-Version Architecture](docs/multiversion-multiloader.md): Architectury and Stonecutter design.
- [Development](docs/development.md): setup, Gradle commands, CI parity, and
  contribution workflow.
- [AGENTS.md](AGENTS.md): short Codex/agent working memory for this repository.

## Safety Notes

- Make world backups before first production use and before upgrades.
- Active wars are persisted under `world/customclaims/`.
- The Xaero module broadcasts active/preparing war markers to all compatible
  clients by default; config can restore side/admin/radius filtering. It still
  validates map war-start requests server-side and never publishes a global
  claim-owner map.

## License

MIT. See [LICENSE](LICENSE).

