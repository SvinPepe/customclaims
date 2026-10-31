# ⚔️ Open Parties and Claims: Warfare

<p align="center">
  <img src="https://cdn.modrinth.com/data/kOE5HGwC/images/ae84c4e4f05a3d020e8060a353519ccdabd0ce44.png" alt="Warfare Showcase" width="100%">
</p>

Turn your peaceful **Open Parties and Claims (OPaC)** server into an epic arena of territorial conquest, tactical sieges, and diplomatic warfare!

**Open Parties and Claims: Warfare** bridges the gap between peaceful claim protection and thrilling faction PvP. Players can declare wars against rival claims, engage in dynamic chunk-by-chunk sieges, track active conflicts on their minimap, and fight for land while keeping offline bases safe with configurable raid windows.

---

## 📚 Guides & Documentation

- 🔄 **[Migration & Upgrade Guide (1.x to 2.0)](https://github.com/SvinPepe/opac-warfare/blob/main/docs/migration.md)** — Step-by-step upgrade instructions from 1.x (1.6.7.2) or early 2.0 builds.
- 📖 **[Server Admin Guide](https://github.com/SvinPepe/opac-warfare/blob/main/docs/server-admin.md)** — Installation, lifecycle, raid hours, and live server operations.
- ⚙️ **[Configuration Reference](https://github.com/SvinPepe/opac-warfare/blob/main/docs/configuration.md)** — Complete options reference, allowed ranges, and defaults for `opac_warfare-*.toml`.
- 🧩 **[Companion Mod: Better Protection](https://github.com/SvinPepe/opac-better-protection)** — Mechanical contraption protection (Create drills/saws), cannon impacts, and `/claimrules`.
- 🏗️ **[Architecture & WarfareBridge](https://github.com/SvinPepe/opac-warfare/blob/main/docs/architecture.md)** — Developer notes, platform abstraction, and external mod integration.

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

## 🧩 Mod Compatibility & Ecosystem

### 🛡️ Official Companion Mod: Better Protection
Looking for Create contraption support, cannon warfare, and custom block-damage rules during sieges?
👉 **[Open Parties and Claims: Better Protection](https://github.com/SvinPepe/opac-better-protection)**

- When installed together, siege zones automatically allow Create drills, cannon impacts (Create Big Cannons), and tactical breaching in actively contested chunks while keeping peaceful claims 100% secure!

### 🌍 Multi-Loader & Multi-Version
Warfare supports your favorite mod loader and version out of the box:
- **Minecraft 1.20.1**: Forge & Fabric
- **Minecraft 1.21.1**: NeoForge & Fabric
- **Minecraft 26.1**: NeoForge

---

## 📦 Installation & Requirements

1. Install **Open Parties and Claims** ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/open-parties-and-claims) / [Modrinth](https://modrinth.com/mod/open-parties-and-claims)) for your loader and Minecraft version.
2. Download the matching **Open Parties and Claims: Warfare** jar:
   - `opac-warfare-1.20.1-forge.jar`
   - `opac-warfare-1.20.1-fabric.jar`
   - `opac-warfare-1.21.1-neoforge.jar`
   - `opac-warfare-1.21.1-fabric.jar`
   - `opac-warfare-26.1-neoforge.jar`
3. Drop the jar into your server's (and client's) `mods/` directory.
4. *(Optional)* Install **Xaero's Minimap** or **Xaero's World Map** for waypoint and map features.

---

## 📖 Commands & Guides

| Command | Permission | Description |
|---|---|---|
| `/war start` | `opac.warfare.start` | Declare war on the eligible border chunk you are standing in. |
| `/war status` | `opac.warfare.status` | Display status and capture progress of your active wars. |
| `/war list` | `opac.warfare.status` | Show active wars and ongoing sieges visible to you. |
| `/war near [radius]` | `opac.warfare.status` | Find wars within a chunk radius (default: 8, max: 32). |
| `/waradmin list` | `opac.warfare.admin` | View all active and preparing wars across all dimensions. |
| `/waradmin stop <uuid\|chunk>` | `opac.warfare.admin` | Force stop a war and restore claims to original owners. |
| `/waradmin setprogress <uuid\|chunk> <0-100>` | `opac.warfare.admin` | Administratively modify capture progress. |
| `/waradmin skip_preparation <chunk>` | `opac.warfare.admin` | Skip the preparation countdown and activate the siege immediately. |

*(Legacy `customclaims.*` permissions remain supported as aliases for backwards compatibility).*

For advanced server configuration, raid window setups, and permission nodes, check our **[Documentation Wiki](https://github.com/SvinPepe/opac-warfare/tree/main/docs)**.
