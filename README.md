# ⚔️ Open Parties and Claims: Warfare

<p align="center">
  <img src="https://cdn.modrinth.com/data/kOE5HGwC/images/ae84c4e4f05a3d020e8060a353519ccdabd0ce44.png" alt="Warfare Showcase" width="100%">
</p>

Turn your peaceful **Open Parties and Claims (OPaC)** server into an epic arena of territorial conquest, tactical sieges, and diplomatic warfare!

[GitHub](https://github.com/SvinPepe/opac-warfare) |
[Modrinth](https://modrinth.com/mod/open-parties-and-claims-warfare) |
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/open-parties-and-claims-warfare) |
[Anonymous metrics](https://customclaims-metrics.svinpepe.workers.dev/)

**Open Parties and Claims: Warfare** bridges the gap between peaceful claim protection and thrilling faction PvP. Players can declare wars against rival claims, engage in dynamic chunk-by-chunk sieges, track active conflicts on their minimap, and fight for land while keeping offline bases safe with configurable raid windows.

---

## 🌟 Key Features

### 🚩 Tactical Chunk Wars & Conquest
- **Border-Based Warfare**: Declare war on neighboring hostile claims from the border. Deep core territory cannot be bypassed without securing border chunks first!
- **Sector Wedge Conquest**: Take control of strategic wedges of connected territory in a single, hard-fought campaign.

### 🛡️ Balanced Capture & Anti-Zerg Mechanics
- **Presence-Driven Capture**: Attackers advance the siege progress bar by holding the contested zone; defenders push it back by defending the territory.
- **Anti-Zerg Protection**: Diminishing returns prevent massive zergs from instantly steamrolling smaller, well-fortified teams. Skill and endurance decide the victor!
- **AFK Protection**: Idle players do not contribute to siege progress.

### ⏰ Fair-Play Raid Windows & Cooldowns
- **No 4 AM Offline Raids**: Define exact server raid hours (e.g., `18:00 - 22:00`) so battles only happen when players are online and ready to fight.
- **Strategic Cooldowns**: Configurable attack and defense cooldowns prevent spam declarations and give defeated parties time to regroup.

<p align="center">
  <img src="https://cdn.modrinth.com/data/kOE5HGwC/images/9d40247bb92110a385003cb2483fae4fe1651c70.png" alt="In-Game Siege Action" width="100%">
</p>

### ❤️ War Lives & Live Scoreboard
- **High-Stakes Combat**: Every warrior receives limited war lives per siege. Lose your lives, and you're out of the battle!
- **Sidebar HUD**: Real-time scoreboard display keeps everyone informed of remaining lives and combat status.

### 🗺️ Full Xaero's Map Integration
- **Fair-Play War Markers**: View active conflict zones and timers directly on **Xaero's Minimap** and **Xaero's World Map**.
- **Right-Click War Start**: Simply right-click any eligible enemy border chunk directly on Xaero's World Map to initiate a siege!

### 🔔 Discord Webhooks
- Automatically broadcast war declarations, phase transitions, and final siege outcomes directly to your server's Discord channel with rich embeds.

---

## ⚔️ How Sieges Work

```text
  [ /war start ] ────► [ Preparation Phase ] ────► [ Active Battle ] ────► [ Resolution ]
  Target border chunk     Defenders & attackers      Fight for zone;           100% = Captured!
  or Xaero right-click    gear up & rally           drain enemy lives         Timeout = Defended!
```

1. **Declaration**: A party declares war on an adjacent rival chunk using `/war start` or the Xaero World Map menu.
2. **Preparation**: A countdown gives defenders time to rally their allies, build fortifications, and prepare gear.
3. **Active Phase**: The chunk becomes contested! Normal claim restrictions relax for combatants:
   - Stand inside the zone to capture or defend.
   - Watch the capture bossbar and sidebar lives.
   - Glowing effects keep combatants visible and prevent hiding in corners.
4. **Resolution**:
   - If attackers reach **100% progress**, the chunk is claimed by the attackers!
   - If defenders hold out until time expires or deplete enemy lives, the siege fails and peace is restored.

---

## 🧩 Separation of Concerns: Warfare vs. Protection

Starting with Warfare 2.0, claim protection rules and mechanics (Create contraption assembly, drills, saws, CBC/Clockwork cannon projectile impacts, explosion protection, and `/claimrules`) have been separated into a dedicated companion mod:

👉 **[Open Parties and Claims: Better Protection](https://github.com/SvinPepe/opac-better-protection)**

- **Open Parties and Claims: Warfare** (this mod) focuses strictly on territorial conquest, contested claims, raid windows, capture progress, war lives, fair-play Xaero waypoint sync, and Discord webhooks.
- When used alongside **Better Protection**, the two mods integrate seamlessly via `WarfareBridge` (`dev.customclaims.api.war.WarfareBridgeHolder`): `opac-better-protection` queries active war status to permit siege damage and contraption breaches inside contested war chunks while preserving claim safety elsewhere.

---

## 🌍 Multi-Loader and Multi-Version Support

Open Parties and Claims: Warfare natively supports multiple mod loaders and Minecraft versions via **Stonecutter** and **Architectury Loom**:

| Minecraft Version | Mod Loader | Java | Distribution Jar | Build Command |
|---|---|---|---|---|
| **1.20.1** | **Forge** (`47.3.0`) | Java 17 | `opac-warfare-1.20.1-forge.jar` | `./gradlew :forge:1.20.1:remapJar` |
| **1.20.1** | **Fabric** (`0.16.14`) | Java 17 | `opac-warfare-1.20.1-fabric.jar` | `./gradlew :fabric:1.20.1:remapJar` |
| **1.21.1** | **NeoForge** (`21.1.232`) | Java 21 | `opac-warfare-1.21.1-neoforge.jar` | `./gradlew :neoforge:1.21.1:remapJar` |
| **1.21.1** | **Fabric** (`0.16.14`) | Java 21 | `opac-warfare-1.21.1-fabric.jar` | `./gradlew :fabric:1.21.1:remapJar` |
| **26.1** | **NeoForge** (`26.1.2.112`) | Java 25 | `opac-warfare-26.1-neoforge.jar` | `./gradlew :neoforge:26.1:assemble` |

---

## 📦 What To Install

Server owners install the distribution jar matching their Minecraft version and loader:
- Forge 1.20.1: `forge/versions/1.20.1/build/libs/opac-warfare-1.20.1-forge.jar`
- Fabric 1.20.1: `fabric/versions/1.20.1/build/libs/opac-warfare-1.20.1-fabric.jar`
- NeoForge 1.21.1: `neoforge/versions/1.21.1/build/libs/opac-warfare-1.21.1-neoforge.jar`
- Fabric 1.21.1: `fabric/versions/1.21.1/build/libs/opac-warfare-1.21.1-fabric.jar`
- NeoForge 26.1: `neoforge/versions/26.1/build/libs/opac-warfare-26.1-neoforge.jar`

Copy the appropriate `opac-warfare-*.jar` into the server `mods/` folder together with Open Parties and Claims.

---

## 🚀 Quick Start

Run test suite:

```powershell
.\gradlew.bat --no-daemon :common:1.21.1:test
```

Build all jars:

```powershell
.\gradlew.bat --no-daemon :neoforge:1.21.1:remapJar :fabric:1.21.1:remapJar :fabric:1.20.1:remapJar :forge:1.20.1:remapJar :neoforge:26.1:assemble
```

On Linux/macOS:

```sh
./gradlew --no-daemon :neoforge:1.21.1:remapJar :fabric:1.21.1:remapJar :fabric:1.20.1:remapJar :forge:1.20.1:remapJar :neoforge:26.1:assemble
```

---

## 📖 Main Commands

| Command | Permission | Description |
|---|---|---|
| `/war start` | `customclaims.war.start` | Declare war on the claimed chunk you are standing in. |
| `/war cancel` | `customclaims.war.cancel` | Cancel an active war declared by your party. |
| `/war info` | `customclaims.war.info` | Display current war status, progress, and remaining lives. |
| `/war top` | `customclaims.war.info` | Show active wars and ongoing sieges across the server. |
| `/waradmin stop` | `customclaims.admin` | Force stop a war in a specific chunk or globally. |
| `/waradmin setprogress` | `customclaims.admin` | Administratively modify capture progress. |

---

## 📊 Anonymous Metrics

On dedicated servers, CustomClaims can send an anonymous heartbeat every 30
minutes to the HTTP(S) endpoint configured as `metrics.endpoint` in
`config/customclaims_core-common.toml`. The default endpoint is
`https://customclaims-metrics.svinpepe.workers.dev/heartbeat`.

Each heartbeat contains only:
- a randomly generated `server_id`, stored locally in `world/customclaims/metrics/server-id.txt`;
- the CustomClaims, Minecraft, and loader versions;
- the current number of online players;
- boolean presence flags for OPaC, Xaero, and Corpse.

It does not include the server IP or hostname, player names, the world seed, or
other identifying data. To disable them, set `metrics.enabled = false` or set `metrics.endpoint = ""` in
`config/customclaims_core-common.toml`.
