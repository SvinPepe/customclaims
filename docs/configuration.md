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
| `permissions.default_player_permissions` | see below | Permission nodes granted to every player without requiring OP or tags. |

Default player permissions:

```toml
permissions.default_player_permissions = [
  "customclaims.war.start",
  "customclaims.war.status",
  "customclaims.explosions.status",
  "customclaims.explosions.toggle",
  "customclaims.create.status",
  "customclaims.create.toggle"
]
```

## `customclaims_war-common.toml`

| Key | Default | Meaning |
| --- | --- | --- |
| `preparation_seconds` | `300` | Seconds between `/war start` and the chunk becoming contested. |
| `max_duration_seconds` | `3600` | Maximum active war duration in seconds. |
| `war.capture.starting_progress` | `50.0` | Capture progress assigned when the active phase starts. |
| `war.capture.player_weight_per_second` | `0.35` | Progress weight per non-AFK participant in the contested chunk each second. |
| `war.capture.attacker_presence_bonus_per_second` | `0.25` | Flat bonus per second when at least one non-AFK attacker is present. |
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

| `war_ui.bossbar_visible_radius_chunks` | `3` | Chunk radius around a war target where players see the war bossbar. |
| `war.contested_owner_uuid` | `"00000000-0000-0000-0000-00000000cc01"` | Fake player UUID for temporary contested OPaC claim ownership. |
| `war.contested_owner_name` | `"Contested War"` | Display name used for the fake contested owner. |
| `war.lives.starting_lives` | `3` | Personal lives assigned to current participants when active phase starts. |
| `war.lives.scoreboard_sidebar_enabled` | `true` | Shows active war lives in the vanilla sidebar scoreboard. |
| `war.lives.scoreboard_objective` | `"cc_war_lives"` | Objective name for the war lives scoreboard. |

### War cooldown windows

Attack and defense windows are stored per `ClaimSideId` (an OPaC party or a solo player side). The first successful `PREPARING` declaration starts the attacker and defender windows at the same instant. Later successful declarations consume another slot without extending either window; when the configured duration expires, the next successful declaration opens a fresh window even if previous slots were unused.

A side can run wars only in one role at a time: an active defense blocks new attacks and an active attack blocks incoming declarations. Raising either chunk limit to `N` allows up to `N` parallel wars in that role. A duration of `0` removes the timed quota, but the corresponding concurrent-war chunk limit remains active.

Daily limits remain optional and are evaluated in addition to cooldowns when configured above `0`.

> Migration: remove `max_active_wars_per_party` and `post_war_protection_seconds` from existing war config files. The former is replaced by the role-specific chunk limits; the latter is replaced by the defender cooldown for the entire side.

## `customclaims_protection-common.toml`

| Key | Default | Meaning |
| --- | --- | --- |
| `foreign_interaction.block_break_limit` | `0` | Runtime-session block break limit for foreign peaceful claims. |
| `foreign_interaction.block_place_limit` | `0` | Runtime-session block place limit for foreign peaceful claims. |
| `foreign_interaction.limit_reset_interval_seconds` | `3600` | Global runtime reset interval for foreign claim counters. |
| `claimrules.toggle_cooldown_seconds` | `600` | Cooldown for side-level `/claimrules` toggles. |
| `explosions.custom_filter_enabled` | `true` | Filters block damage from explosions in protected claimed chunks. |
| `explosions.allow_in_war_chunks` | `true` | Does not filter explosion block damage in contested war chunks. |
| `big_cannons.block_projectile_launch_from_protected_claims` | `true` | Cancels Create Big Cannons projectiles spawned from protected claimed chunks. |
| `big_cannons.log_blocked_projectiles` | `true` | Logs Create Big Cannons projectiles blocked by explosion protection. |
| `storage_rules.allow_open_storage_on_foreign_claims` | `true` | Allows opening storage on foreign peaceful claims. |
| `storage_rules.protect_storage_from_breaking_on_peaceful_claims` | `true` | Protects configured storage blocks from breaking on foreign peaceful claims. |
| `storage_rules.allow_storage_breaking_in_war_chunks` | `true` | Allows protected storage blocks to be broken in contested chunks. |
| `storage_rules.protected_storage_blocks` | see below | Storage block ids protected on peaceful foreign claims. |
| `wither_rules.disable_wither_summon_in_overworld` | `true` | Blocks Wither summoning in the Overworld. |
| `wither_rules.disable_wither_summon_in_end` | `true` | Blocks Wither summoning in the End. |
| `wither_rules.disable_wither_summon_in_nether` | `false` | Blocks Wither summoning in the Nether. |
| `wither_rules.log_blocked_wither_summons` | `true` | Logs blocked Wither summons. |
| `villager_protection.enable_villager_protection` | `true` | Enables villager/trader damage protection. |
| `villager_protection.protect_villagers_everywhere` | `true` | Protects villagers/traders outside claims too. |
| `villager_protection.protect_villagers_only_on_claims` | `false` | Restricts villager/trader protection to claims. |
| `villager_protection.protect_wandering_traders` | `true` | Includes wandering traders in protection rules. |
| `villager_protection.prevent_player_damage` | `true` | Blocks player damage to protected villagers/traders. |
| `villager_protection.prevent_fire_damage` | `true` | Blocks fire damage to protected villagers/traders. |
| `villager_protection.prevent_lava_damage` | `true` | Blocks lava damage to protected villagers/traders. |
| `villager_protection.prevent_fall_damage` | `true` | Blocks fall damage to protected villagers/traders. |
| `villager_protection.prevent_drowning_damage` | `true` | Blocks drowning damage to protected villagers/traders. |
| `villager_protection.prevent_suffocation_damage` | `true` | Blocks suffocation damage to protected villagers/traders. |
| `villager_protection.prevent_explosion_damage` | `true` | Blocks explosion damage to protected villagers/traders. |
| `villager_protection.prevent_projectile_damage` | `true` | Blocks projectile damage to protected villagers/traders. |
| `villager_protection.prevent_void_damage` | `false` | Blocks void damage to protected villagers/traders. |
| `villager_protection.allow_zombie_damage` | `true` | Allows zombie damage even when villager protection is enabled. |
| `villager_protection.protect_villagers_in_war_chunks` | `false` | If `false`, disables villager/trader protection in contested chunks. |
| `villager_protection.allow_player_damage_to_villagers_in_war_chunks` | `true` | Allows player damage to villagers/traders in contested chunks. |
| `villager_protection.allow_explosion_damage_to_villagers_in_war_chunks` | `true` | Allows explosion damage to villagers/traders in contested chunks. |
| `villager_protection.clear_fire_on_blocked_damage` | `true` | Clears fire when protected damage is blocked. |

The runtime `/claimrules create` side toggle is persisted in `protection/create-machines.txt`, not TOML. It controls Create drills, saws, other block-breaking movement machines, and Aeronautics/Offroad bore mining.

The independent `/claimrules assembly` side toggle is persisted in `protection/create-assemblies.txt`. It controls Create and optional Sable contraption assembly. On the first upgrade that creates this file, existing `create` values and their cooldowns are copied to `assembly`.

Default protected storage blocks:

```toml
storage_rules.protected_storage_blocks = [
  "minecraft:chest",
  "minecraft:trapped_chest",
  "minecraft:barrel",
  "minecraft:shulker_box"
]
```

## `customclaims_xaero-common.toml`

| Key | Default | Meaning |
| --- | --- | --- |
| `xaero_overlay.visible_radius_chunks` | `8` | War markers are sent only for visible wars within this chunk radius. |
| `xaero_overlay.custom_overlay_enabled` | `false` | Renders the old CustomClaims client HUD overlay when enabled. |
| `xaero_waypoints.enabled` | `true` | Creates Xaero temporary waypoints from visible war markers on compatible clients. |
| `xaero_waypoints.refresh_interval_seconds` | `5` | Minimum seconds between refreshing the same temporary war waypoint. |
| `xaero_map_war_start.enabled` | `true` | Allows players with `customclaims.war.start` to start wars from the Xaero World Map right-click menu. |
| `xaero_map_war_start.max_distance_chunks` | `32` | Maximum same-dimension chunk distance for Xaero map war-start requests. |

## Operational Defaults To Review

- Keep `war.contested_owner_uuid` stable unless you are intentionally migrating
  contested claim ownership behavior.
- Tighten `permissions.default_player_permissions` if normal players should not
  start wars or toggle side rules.
- Review `raid_window.timezone` and `raid_window.blocked_windows` before public
  use; the default timezone is `Europe/Moscow`.
- `foreign_interaction.block_break_limit = 0` and
  `foreign_interaction.block_place_limit = 0` mean foreign peaceful block
  break/place actions are not allowed by the limit service.
