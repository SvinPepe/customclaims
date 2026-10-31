# Migration & Upgrade Guide

This guide explains how to cleanly upgrade to **Open Parties and Claims: Warfare 2.0** from earlier releases:
- **Upgrading from CustomClaims 1.x (e.g. 1.6.7.2)**
- **Upgrading from early/unstable 2.0.0 builds**

---

## 🧩 Architectural Shift in 2.0: Warfare vs. Protection

Starting with version 2.0, claim protection rules have been separated into a dedicated companion mod:
- **Open Parties and Claims: Warfare** (this mod):
  Focuses strictly on territorial conquest, contested war zones, sector wedges, anti-zerg capture mechanics, raid windows, war lives, fair-play Xaero map integration, and Discord webhooks.
- **[Open Parties and Claims: Better Protection](https://github.com/SvinPepe/opac-better-protection)** (companion mod):
  Handles block/entity protection from mechanical contraptions (Create drills, saws, deployers), CBC/Clockwork cannon projectile impacts, explosion protection toggles, and the `/claimrules` GUI/commands.

When both mods are installed on a server, they communicate seamlessly via the built-in `WarfareBridge` (`dev.customclaims.api.war.WarfareBridgeHolder`). During active sieges, protection rules allow siege destruction only inside contested chunks while keeping all peaceful claims fully shielded.

---

## 1. Upgrading from 1.6.7.2 (or any 1.x version)

### Step 1: Pre-Upgrade Preparation
1. Make a backup of your world and server files.
2. In-game or via console, ensure no active wars are running (`/waradmin stop` if needed), so that all contested chunks are restored to their original owners.

### Step 2: Replace Mod Jars in `mods/`
1. Remove `customclaims-1.6.7.2.jar` (or earlier).
2. Add the corresponding `opac-warfare-2.0.0-<loader>.jar` for your loader (Forge 1.20.1, Fabric 1.20.1/1.21.1, NeoForge 1.21.1/26.1).
3. **If you use Create, cannons, or `/claimrules`**: download and place the companion mod `opac-better-protection-*.jar` into `mods/`.

### Step 3: Clean Up Config Files in `config/`
Old config files were named `customclaims-*.toml`. In 2.0, configs are renamed to match the mod identifier:
- `config/opac_warfare-common.toml`
- `config/opac_warfare-war.toml`
- `config/opac_warfare-xaero.toml`

Remove or archive the old `customclaims-*.toml` files. The server will generate fresh, cleanly formatted configuration files on first boot.

### Step 4: Update Permissions
Warfare permission nodes now use the modern `opac.warfare.*` prefix:
- `/war start` ➔ `opac.warfare.start`
- `/war status`, `/war list`, `/war near` ➔ `opac.warfare.status`
- `/waradmin *` ➔ `opac.warfare.admin`

*(Legacy `customclaims.*` aliases are still accepted for backwards compatibility, but transitioning to `opac.warfare.*` is recommended).*

---

## 2. Upgrading from Initial 2.0.0 Builds

If you installed an early build of 2.0.0, apply this hotfix upgrade to resolve configuration duplicate conflicts and decouple contested claims from server spawn claims:

### Step 1: Replace Jar
Replace the old 2.0.0 jar in your `mods/` folder with the updated build.

### Step 2: Regenerate `opac_warfare-war.toml`
Delete `config/opac_warfare-war.toml` so the server generates the new settings:
- `war.discord.webhook_language = "en_us"` (default is now English).
- `[war.contested_claim]` section:
  ```toml
  [war.contested_claim]
      # UUID used as the fake player owner of contested war chunks.
      owner_uuid = "a9d1645e-b816-3b60-b6d4-d5581ff24009"
      # Display name for the contested territory fake player claim.
      name = "Contested Territory"
      # Color (ARGB integer) for contested territory claims on maps. Default is -3342336 (0xFFCC0000).
      color = -3342336
  ```

### Step 3: Dedicated Contested Claim Owner (Wilderness Rules)
In 2.0.0+, contested chunks are no longer assigned to `Server` (`00000000-0000-0000-0000-000000000000`). Instead, they are owned by a dedicated fake player identity (`a9d1645e-b816-3b60-b6d4-d5581ff24009`).

On server start, Warfare automatically generates:
`world/data/openpartiesandclaims/player-configs/a9d1645e-b816-3b60-b6d4-d5581ff24009.toml`
with `protectClaimedChunks = false`. This grants full wilderness rules (anyone can build, break, and fight) inside active war zones without weakening actual server claims (e.g. spawn claims).

---

## 3. Command Reference in 2.0

### Player Commands
| Command | Permission | Description |
|---|---|---|
| `/war start` | `opac.warfare.start` | Declares war on the border chunk where the player is standing. |
| `/war status` | `opac.warfare.status` | Displays status and capture progress of ongoing wars. |
| `/war list` | `opac.warfare.status` | Lists all visible preparing and active wars. |
| `/war near [radius]` | `opac.warfare.status` | Searches for active wars within a chunk radius (default 8, max 32). |

### Admin Commands
| Command | Permission | Description |
|---|---|---|
| `/waradmin list` | `opac.warfare.admin` | Lists all active and preparing wars across dimensions. |
| `/waradmin stop <uuid\|chunk>` | `opac.warfare.admin` | Cancels a war and restores original claim snapshots. |
| `/waradmin setprogress <uuid\|chunk> <0-100>` | `opac.warfare.admin` | Sets the capture progress of an active war. |
| `/waradmin skip_preparation <chunk>` | `opac.warfare.admin` | Immediately starts the active siege phase. |
