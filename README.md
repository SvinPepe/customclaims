# Open Parties and Claims: Warfare

**Open Parties and Claims: Warfare** is an unofficial NeoForge addon for
[Open Parties and Claims](https://modrinth.com/mod/open-parties-and-claims).

It adds chunk wars, contested claims, side-based attack and defense cooldowns with configurable target-chunk slots, configurable claim protection rules,
Create mining and contraption-assembly controls, Aeronautics/Offroad bore protection,
Sable assembly protection, Create Big Cannons protection, and globally visible Xaero war markers with configurable filtering. Open Parties and Claims
remains the source of party membership and claim ownership; this project builds
warfare and protection mechanics on top of OPaC territories.

## Requirements

- Minecraft `1.21.1` baseline; metadata allows experimental `[1.21.1,1.27)` probes
- NeoForge `21.1.232` baseline; metadata allows experimental `[21.1.232,27.0)` probes
- Java `21`
- Open Parties and Claims `neoforge-1.21.1-0.27.5` or newer on the baseline stack

The official jar is one modern artifact. `Minecraft 1.21.1 + NeoForge 21.1.232`
is the fully tested baseline; NeoForge `21.x` through `26.x` is experimental
same-jar support that must pass boot and gameplay smoke tests before being
called verified. Forge, Fabric, Quilt, older `1.20.x`, and Minecraft `1.27+`
are not promised. See [Compatibility](docs/compatibility.md) for the support
policy and probe workflow.

Optional integrations activate only when their target mods are installed:

- Create `mc1.21.1-6.0.9` or newer
- Create Aeronautics/Offroad `1.3.0` or newer for bore protection
- Sable `2.0.3` for optional contraption assembly protection (with Sable's required Create version)
- Create Big Cannons `5.11.7` or newer
- Xaero Minimap or Xaero World Map on compatible clients; Xaero World Map
  also enables a right-click map war-start action

## What To Install

Server owners normally install the single distribution jar built by the
`opac-warfare` module. That jar contains the seven functional modules:

- `customclaims_core`
- `customclaims_war`
- `customclaims_protection`
- `customclaims_create`
- `customclaims_aeronautics`
- `customclaims_big_cannons`
- `customclaims_xaero`

The module jars can also be built separately for development, but the public
server artifact is:

```text
opac-warfare/build/libs/opac-warfare-<version>.jar
```

For this release, that artifact is `opac-warfare-1.6.5.jar`.

## Quick Start

Build the same artifact that CI uploads:

```powershell
.\gradlew.bat --no-daemon build :opac-warfare:jar
```

On Linux/macOS:

```sh
./gradlew --no-daemon build :opac-warfare:jar
```

Copy the resulting `opac-warfare-*.jar` into the server `mods/` folder together
with Open Parties and Claims. Install optional compat mods only when you want
their integrations.

After first server start, NeoForge writes common config files under `config/`.
See [Configuration](docs/configuration.md) before using the mod on a production
world.

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

## Documentation

- [Server Admin Guide](docs/server-admin.md): installation, gameplay flow,
  commands, permissions, runtime data, and operational notes.
- [Compatibility](docs/compatibility.md): supported Minecraft/NeoForge target,
  one-jar policy, and adjacent-version probe workflow.
- [Configuration](docs/configuration.md): all common config files and keys.
- [Architecture](docs/architecture.md): modules, services, event entrypoints,
  persistence, and compat boundaries.
- [Development](docs/development.md): setup, Gradle commands, CI parity, and
  contribution workflow.
- [AGENTS.md](AGENTS.md): short Codex/agent working memory for this repository.

## Safety Notes

- Make world backups before first production use and before upgrades.
- Active wars and claim-rule toggles are persisted under
  `world/customclaims/`.
- The Xaero module broadcasts active/preparing war markers to all compatible
  clients by default; config can restore side/admin/radius filtering. It still
  validates map war-start requests server-side and never publishes a global
  claim-owner map.
- Create, Aeronautics/Offroad, Sable, and Create Big Cannons hooks are guarded by
  mod-load checks and stay inactive when the target mods are absent.

## License

MIT. See [LICENSE](LICENSE).
