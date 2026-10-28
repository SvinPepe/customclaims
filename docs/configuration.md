# Configuration

NeoForge writes common config files under the server `config/` directory. The
file names are based on each module mod id, for example
`customclaims_war-common.toml`.

Values below are the defaults verified directly from the Java config classes (`CoreConfig.java`, `WarConfig.java`, `XaeroCompatConfig.java`).

## `customclaims_core-common.toml`

| Key | Default | Allowed Range | Meaning |
| --- | --- | --- | --- |
| `debug_logging` | `false` | `true` / `false` | Enables additional server-side Custom Claims debug logging. |
| `op_permission_level` | `2` | `0` .. `4` | Operator level accepted by the permission service. |
| `metrics.enabled` | `true` | `true` / `false` | Enables anonymous heartbeat metrics on dedicated servers. |
| `metrics.endpoint` | `"https://customclaims-metrics.svinpepe.workers.dev/heartbeat"` | valid URL or `""` | HTTP(S) endpoint that receives anonymous metrics heartbeats. Empty string disables heartbeat sending. |
| `permissions.default_player_permissions` | see below | list of strings | Permission nodes granted to every player without requiring OP or tags. |

Default player permissions:

```toml
permissions.default_player_permissions = [
  "opac.warfare.start",
  "opac.warfare.status",
  "opac.protection.claimrules",
  "opac.protection.explosions.status",
  "opac.protection.explosions.toggle",
  "opac.protection.create.status",
  "opac.protection.create.toggle",
  "opac.protection.assembly.status",
  "opac.protection.assembly.toggle",
  "customclaims.war.start",
  "customclaims.war.status",
  "customclaims.claimrules",
  "customclaims.explosions.status",
  "customclaims.explosions.toggle",
  "customclaims.create.status",
  "customclaims.create.toggle",
  "customclaims.assembly.status",
  "customclaims.assembly.toggle"
]
```

> **Note**: Warfare permissions (`opac.warfare.start`, `opac.warfare.status` and legacy aliases) control warfare actions in this mod. Legacy protection permissions (`opac.protection.*`) are retained in the core config defaults for backward compatibility with existing server configs and companion mod interoperability (`Open Parties and Claims: Better Protection`).

## `customclaims_war-common.toml`

| Key | Default | Allowed Range | Meaning |
| --- | --- | --- | --- |
| `preparation_seconds` | `300` | `5` .. `86400` | Seconds between `/war start` and the chunk sector becoming contested. |
| `max_duration_seconds` | `3600` | `60` .. `604800` | Maximum active war duration in seconds. |
| `war.sector.max_chunks` | `4` | `1` .. `6` | Maximum connected claim chunks captured in one conquest wedge sector. |
| `war.sector.max_conquest_depth_chunks` | `2` | `1` .. `4` | Maximum conquest depth from border/wilderness (prevents snake rushes into deep base core). |
| `war.capture.starting_progress` | `50.0` | `0.0` .. `100.0` | Capture progress assigned when the active phase starts. |
| `war.capture.base_rate_per_second` | `0.40` | `0.0` .. `100.0` | Base capture speed per second given by 1 non-AFK attacker in sector. |
| `war.capture.extra_attacker_bonus_per_second` | `0.05` | `0.0` .. `100.0` | Diminishing bonus capture speed per additional non-AFK attacker. |
| `war.capture.max_capture_rate_per_second` | `0.60` | `0.0` .. `100.0` | Anti-zerg hard cap on total positive capture rate per second. |
| `war.capture.defender_weight_per_second` | `0.35` | `0.0` .. `100.0` | Progress reduction per second per defending player inside the sector. |
| `war.capture.glowing_effect_enabled` | `true` | `true` / `false` | Applies Glowing (`MobEffects.GLOWING`) when attackers and defenders contest the sector. |
| `war.capture.empty_chunk_decay_per_second` | `0.50` | `0.0` .. `100.0` | Progress decay per second when no non-AFK attackers or defenders are present. |
| `war.discord.webhook_url` | `""` | string | Discord channel webhook URL; empty string disables webhook notifications. |
| `war.discord.webhook_language` | `"ru_ru"` | `"ru_ru"` / `"en_us"` | Language for Discord webhook embed notifications. |
| `war.cooldown.attacker_seconds` | `7200` | `0` .. `604800` | Fixed attack window opened by a side's first successful declaration; `0` disables the timed window. |
| `war.cooldown.defender_seconds` | `3600` | `0` .. `604800` | Fixed defense/protection window opened by a side's first accepted incoming declaration; `0` disables the timed window. |
| `war.cooldown.max_started_chunks_per_attacker_side` | `1` | `1` .. `1024` | Maximum successful starts in one attack window and maximum concurrent wars for an attacking side. |
| `war.cooldown.max_accepted_chunks_per_defender_side` | `1` | `1` .. `1024` | Maximum accepted starts in one defense window and maximum concurrent wars for a defending side. |
| `war.daily_start_limit.max_started_chunks_per_attacker_side` | `0` | `0` .. `1024` | Optional maximum successful target starts per attacking side per configured day; `0` disables the daily limit. |
| `war.daily_start_limit.max_accepted_chunks_per_defender_side` | `0` | `0` .. `1024` | Optional maximum successful incoming target starts per defending side per configured day; `0` disables the daily incoming limit. |
| `allow_diagonal_border_chunks` | `false` | `true` / `false` | Whether diagonal adjacency counts for border checks. |
| `afk_seconds` | `300` | `30` .. `86400` | Players with no tracked interaction for this long are treated as AFK. |
| `raid_window.enable_raid_window` | `true` | `true` / `false` | Blocks `/war start` inside configured raid windows. |
| `raid_window.timezone` | `"Europe/Moscow"` | valid timezone ID | Timezone used to evaluate raid windows. |
| `raid_window.blocked_windows` | `["04:00-08:00"]` | list of `HH:mm-HH:mm` | Blocked windows during which war declarations are prevented. |
| `raid_window.allow_ongoing_wars_to_continue_after_window_start` | `true` | `true` / `false` | If `false`, active wars fail when a blocked raid window starts. |
| `war_ui.bossbar_visible_radius_chunks` | `3` | `0` .. `64` | When global bossbars are disabled, nearby same-dimension players see them within this radius; involved sides always see them. |
| `war.notifications.broadcast_to_all_players` | `true` | `true` / `false` | Sends every war lifecycle notification to every online player; `false` restricts them to the involved sides. |
| `war_ui.bossbar_visible_to_all_players` | `true` | `true` / `false` | Shows single frontline war conquest bossbar to every online player regardless of distance or dimension. |
| `war.lives.starting_lives` | `3` | `1` .. `100` | Personal lives assigned to current participants when active phase starts. |
| `war.lives.scoreboard_sidebar_enabled` | `true` | `true` / `false` | Shows active war lives in the vanilla sidebar scoreboard. |
| `war.lives.scoreboard_objective` | `"cc_war_lives"` | string | Objective name for the war lives scoreboard. |

Contested chunks always use OPaC's built-in `Server` owner
(`00000000-0000-0000-0000-000000000000`); this is intentionally not configurable.

### War cooldown windows

Attack and defense windows are stored per `ClaimSideId` (an OPaC party or a solo player side). The first successful `PREPARING` declaration starts the attacker and defender windows at the same instant. Later successful declarations consume another slot without extending either window; when the configured duration expires, the next successful declaration opens a fresh window even if previous slots were unused.

A side can run wars only in one role at a time: an active defense blocks new attacks and an active attack blocks incoming declarations. Raising either chunk limit to `N` allows up to `N` parallel wars in that role. A duration of `0` removes the timed quota, but the corresponding concurrent-war chunk limit remains active.

Daily limits remain optional and are evaluated in addition to cooldowns when configured above `0`.

> Migration: remove `max_active_wars_per_party` and `post_war_protection_seconds` from existing war config files. The former is replaced by the role-specific chunk limits; the latter is replaced by the defender cooldown for the entire side.

> Migration: remove the obsolete `war.contested_owner_uuid` and `war.contested_owner_name` keys. Active wars are reassigned to OPaC's built-in `Server` owner when loaded.

## `customclaims_xaero-common.toml`

| Key | Default | Allowed Range | Meaning |
| --- | --- | --- | --- |
| `xaero_overlay.visible_to_all_players` | `true` | `true` / `false` | Sends every active/preparing war marker to every compatible player. Disable to restore side/admin/radius filtering. |
| `xaero_overlay.visible_radius_chunks` | `8` | `0` .. `32` | When global marker visibility is disabled, nearby players receive markers within this radius. |
| `xaero_overlay.custom_overlay_enabled` | `false` | `true` / `false` | Renders the old CustomClaims client HUD overlay when enabled. |
| `xaero_waypoints.enabled` | `true` | `true` / `false` | Creates Xaero temporary waypoints from visible war markers on compatible clients. |
| `xaero_waypoints.refresh_interval_seconds` | `5` | `1` .. `300` | Minimum seconds between refreshing the same temporary war waypoint. |
| `xaero_map_war_start.enabled` | `true` | `true` / `false` | Allows players with `opac.warfare.start` (or legacy `customclaims.war.start`) to start wars from the Xaero World Map right-click menu. |
| `xaero_map_war_start.max_distance_chunks` | `32` | `0` .. `32` | Maximum same-dimension Chebyshev chunk distance for Xaero map war-start requests. `0` limits it to the player's current chunk. |

## Companion Mod: Better Protection

For claim protection configuration (Create drills/saws/assembly, CBC cannons, explosion rules, and `/claimrules`), install and configure the companion mod **[Open Parties and Claims: Better Protection](https://github.com/SvinPepe/opac-better-protection)** (`opac_protection-common.toml`).

## Operational Defaults To Review

- Tighten `permissions.default_player_permissions` if normal players should not start wars.
- Review `raid_window.timezone` and `raid_window.blocked_windows` before public use; the default timezone is `Europe/Moscow`.
- Review `metrics.enabled` and `metrics.endpoint` if you wish to adjust or disable anonymous server heartbeats.


