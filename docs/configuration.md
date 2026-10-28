# Configuration

NeoForge writes common config files under the server `config/` directory. The
file names are based on each module mod id, for example
`customclaims_war-common.toml`.

Values below are the defaults from the current Java config classes.

## `customclaims_core-common.toml`

| Key | Default | Meaning |
| --- | --- | --- |
| `debug_logging` | `false` | Enables additional server-side Custom Claims debug logging. |
| `op_permission_level` | `2` | Operator level accepted by the permission service. |
| `metrics.enabled` | `true` | Enables anonymous heartbeat metrics on dedicated servers. |
| `metrics.endpoint` | `""` | HTTP(S) heartbeat endpoint; empty means no request is sent. |
| `permissions.default_player_permissions` | see below | Permission nodes granted to every player without requiring OP or tags. |

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

## `customclaims_war-common.toml`

| Key | Default | Meaning |
| --- | --- | --- |
| `preparation_seconds` | `300` | Seconds between `/war start` and the chunk sector becoming contested. |
| `max_duration_seconds` | `3600` | Maximum active war duration in seconds. |
| `war.sector.max_chunks` | `4` | Maximum connected claim chunks captured in one conquest wedge sector (1-6). |
| `war.sector.max_conquest_depth_chunks` | `2` | Maximum conquest depth from border/wilderness (prevents snake rushes into deep base core). |
| `war.capture.starting_progress` | `50.0` | Capture progress assigned when the active phase starts. |
| `war.capture.base_rate_per_second` | `0.40` | Base capture speed per second given by 1 non-AFK attacker in sector. |
| `war.capture.extra_attacker_bonus_per_second` | `0.05` | Diminishing bonus capture speed per additional non-AFK attacker. |
| `war.capture.max_capture_rate_per_second` | `0.60` | Anti-zerg hard cap on total positive capture rate per second. |
| `war.capture.defender_weight_per_second` | `0.35` | Progress reduction per second per defending player inside the sector. |
| `war.capture.glowing_effect_enabled` | `true` | Applies Glowing (MobEffects.GLOWING) when attackers and defenders contest the sector. |
| `war.capture.empty_chunk_decay_per_second` | `0.50` | Progress decay per second when no non-AFK attackers or defenders are present. |
| `war.cooldown.attacker_seconds` | `7200` | Fixed attack window opened by a side's first successful declaration; `0` disables the timed window. |
| `war.cooldown.defender_seconds` | `3600` | Fixed defense/protection window opened by a side's first accepted incoming declaration; `0` disables the timed window. |
| `war.cooldown.max_started_chunks_per_attacker_side` | `1` | Maximum successful starts in one attack window and maximum concurrent wars for an attacking side. |
| `war.cooldown.max_accepted_chunks_per_defender_side` | `1` | Maximum accepted starts in one defense window and maximum concurrent wars for a defending side. |
| `war.daily_start_limit.max_started_chunks_per_attacker_side` | `0` | Optional maximum successful target starts per attacking side per configured day; `0` disables the daily limit. |
| `war.daily_start_limit.max_accepted_chunks_per_defender_side` | `0` | Optional maximum successful incoming target starts per defending side per configured day; `0` disables the daily incoming limit. |
| `allow_diagonal_border_chunks` | `false` | Whether diagonal adjacency counts for border checks. |
| `afk_seconds` | `300` | Players with no tracked interaction for this long are treated as AFK. |
| `raid_window.enable_raid_window` | `true` | Blocks `/war start` inside configured raid windows. |
| `raid_window.timezone` | `"Europe/Moscow"` | Timezone used to evaluate raid windows. |
| `raid_window.blocked_windows` | `["04:00-08:00"]` | Blocked windows in `HH:mm-HH:mm` form. |
| `raid_window.allow_ongoing_wars_to_continue_after_window_start` | `true` | If `false`, active wars fail when a blocked raid window starts. |
| `war.notifications.broadcast_to_all_players` | `true` | Sends every war lifecycle notification to every online player; `false` restricts them to the involved sides. |
| `war.discord.webhook_url` | `""` | Discord channel webhook URL; empty string disables webhook notifications. |
| `war.discord.webhook_language` | `"ru_ru"` | Language for Discord webhook embed notifications ('ru_ru' or 'en_us'). |
| `war_ui.bossbar_visible_to_all_players` | `true` | Shows single frontline war conquest bossbar to every online player regardless of distance or dimension. |
| `war_ui.bossbar_visible_radius_chunks` | `3` | When global bossbars are disabled, nearby same-dimension players see them within this radius; involved sides always see them. |
| `war.lives.starting_lives` | `3` | Personal lives assigned to current participants when active phase starts. |
| `war.lives.scoreboard_sidebar_enabled` | `true` | Shows active war lives in the vanilla sidebar scoreboard. |
| `war.lives.scoreboard_objective` | `"cc_war_lives"` | Objective name for the war lives scoreboard. |

Contested chunks always use OPaC's built-in `Server` owner
(`00000000-0000-0000-0000-000000000000`); this is intentionally not configurable.

### War cooldown windows

Attack and defense windows are stored per `ClaimSideId` (an OPaC party or a solo player side). The first successful `PREPARING` declaration starts the attacker and defender windows at the same instant. Later successful declarations consume another slot without extending either window; when the configured duration expires, the next successful declaration opens a fresh window even if previous slots were unused.

A side can run wars only in one role at a time: an active defense blocks new attacks and an active attack blocks incoming declarations. Raising either chunk limit to `N` allows up to `N` parallel wars in that role. A duration of `0` removes the timed quota, but the corresponding concurrent-war chunk limit remains active.

Daily limits remain optional and are evaluated in addition to cooldowns when configured above `0`.

## `customclaims_protection-common.toml`

| Key | Default | Meaning |
| --- | --- | --- |
| `foreign_interaction.block_break_limit` | `0` | Runtime-session block break limit for foreign peaceful claims. |
| `foreign_interaction.block_place_limit` | `0` | Runtime-session block place limit for foreign peaceful claims. |
| `foreign_interaction.limit_reset_interval_seconds` | `3600` | Global runtime reset interval for foreign claim counters. |
| `entity_interaction.contested_exceptions` | `["corpse:corpse"]` | Exact entity type IDs whose right-click interactions grant an OPaC bypass to attacking/defending sides in the target entity's contested chunk. |
| `claimrules.toggle_cooldown_seconds` | `600` | Cooldown for side-level `/claimrules` toggles. |
| `explosions.custom_filter_enabled` | `true` | Filters block damage from explosions in protected claimed chunks via direct OPAC API sync. |
| `explosions.allow_in_war_chunks` | `true` | Does not filter explosion block damage in contested war chunks (via IWarfareBridge soft hook). |
| `big_cannons.block_projectile_launch_from_protected_claims` | `true` | Cancels Create Big Cannons projectiles spawned from protected claimed chunks and displays an actionbar alert. |
| `big_cannons.log_blocked_projectiles` | `true` | Logs Create Big Cannons projectiles blocked by explosion protection. |

The runtime `/claimrules create` side toggle is persisted in `protection/create-machines.txt`. It controls foreign Create machines (drills, saws, deployers) across borders while allowing claim owners/members to freely use their machines inside their claims.

`entity_interaction.contested_exceptions` accepts exact `namespace:path` IDs, not
wildcards or tags. An empty list (`[]`) disables these exceptions. IDs for mods
that are not installed are harmless; Corpse is not a required dependency. The
server reads the current list on each interaction, including after a config reload.
For example:

```toml
[entity_interaction]
contested_exceptions = ["corpse:corpse", "minecraft:chest_minecart"]
```

Only participants of the war at the target entity's chunk receive this bypass.
Outsiders, peaceful claims, and unclaimed chunks receive no new pass. This handles
right-click interactions only and preserves the entity mod's own ownership/access
rules and event cancellations. Like the existing block bypass, it grants a full
OPaC pass until the next server tick, so the pass is broader than one interaction
while active. Clearing the list prevents new grants; an existing pass expires on
the next tick.

The independent `/claimrules assembly` side toggle is persisted in `protection/create-assemblies.txt`. It controls Create and optional Sable contraption assembly. On the first upgrade that creates this file, existing `create` values and their cooldowns are copied to `assembly`.

Wither blocking and villager/trader damage protection are opt-in. With the defaults above, CustomClaims neither cancels Wither spawns nor villager/trader damage, leaving vanilla and OPaC mechanics unchanged.

NeoForge preserves values already present in server config files. Existing servers that want the new passive behavior must set all three `wither_rules.disable_wither_summon_in_*` values and `villager_protection.enable_villager_protection` to `false`, or regenerate the protection config.

The removed `storage_rules.*` keys are obsolete and can be deleted from existing config files. Storage access and breaking are now governed by vanilla and OPaC only.

## `customclaims_xaero-common.toml`

| Key | Default | Meaning |
| --- | --- | --- |
| `xaero_overlay.visible_to_all_players` | `true` | Sends every active/preparing war marker to every compatible player. Disable to restore side/admin/radius filtering. |
| `xaero_overlay.visible_radius_chunks` | `8` | When global marker visibility is disabled, nearby players receive markers within this radius. |
| `xaero_overlay.custom_overlay_enabled` | `false` | Renders the old CustomClaims client HUD overlay when enabled. |
| `xaero_waypoints.enabled` | `true` | Creates Xaero temporary waypoints from visible war markers on compatible clients. |
| `xaero_waypoints.refresh_interval_seconds` | `5` | Minimum seconds between refreshing the same temporary war waypoint. |
| `xaero_map_war_start.enabled` | `true` | Allows players with `customclaims.war.start` to start wars from the Xaero World Map right-click menu. |
| `xaero_map_war_start.max_distance_chunks` | `32` | Maximum same-dimension chunk distance for Xaero map war-start requests. |

## Operational Defaults To Review

- Tighten `permissions.default_player_permissions` if normal players should not
  start wars or toggle side rules.
- Review `raid_window.timezone` and `raid_window.blocked_windows` before public
  use; the default timezone is `Europe/Moscow`.
- `foreign_interaction.block_break_limit = 0` and
  `foreign_interaction.block_place_limit = 0` mean foreign peaceful block
  break/place actions are not allowed by the limit service.
