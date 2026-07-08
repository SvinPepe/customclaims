# Open Parties and Claims: Warfare

**Open Parties and Claims: Warfare** is an unofficial NeoForge addon for
[Open Parties and Claims](https://modrinth.com/mod/open-parties-and-claims).

It adds chunk wars, contested claims, configurable claim protection rules,
Create machine controls, Aeronautics/Offroad bore protection, Create Big
Cannons protection, and fair-play Xaero war markers. Open Parties and Claims
remains the source of party membership and claim ownership; this project builds
warfare and protection mechanics on top of OPaC territories.

## Requirements

- Minecraft `1.21.1`
- NeoForge `21.1.x`; the build is anchored on `21.1.232`
- Java `21`
- Open Parties and Claims `neoforge-1.21.1-0.27.5` or newer

The official jar is a single modern artifact for the `Minecraft 1.21.1 +
NeoForge 21.1.x` ecosystem. Older `1.20.x`, newer `1.21.x`, `26.x`, Forge,
Fabric, and Quilt targets are not promised by this jar. See
[Compatibility](docs/compatibility.md) for the support policy and probe
workflow.

Optional integrations activate only when their target mods are installed:

- Create `mc1.21.1-6.0.9` or newer
- Create Aeronautics/Offroad `1.3.0` or newer for bore protection
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
and distance limits, and all normal war target rules.

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
- The Xaero module sends only fair-play war markers to eligible clients and
  validates map war-start requests server-side; it does not publish a global
  claim-owner map.
- Create, Aeronautics/Offroad, and Create Big Cannons hooks are guarded by
  mod-load checks and stay inactive when the target mods are absent.

## License

MIT. See [LICENSE](LICENSE).
