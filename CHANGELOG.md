# Changelog

## 1.6.5

### Added

* Added persistent side-based attack and defense cooldown windows for war declarations.
* Added configurable attack/defense cooldown durations and per-role target-chunk limits.
* Added `war/side-cooldowns.dat` to persist cooldown windows and consumed slots across restarts.

### Changed

* Updated the mod version to `1.6.5`.
* Disabled daily outgoing and incoming war limits by default while keeping them available as optional additional quotas.
* Replaced chunk-level post-war protection with defender-side protection that applies to all claims owned by that side.
* Replaced `max_active_wars_per_party` with role-specific cooldown chunk limits and prevented a side from attacking while defending or defending while attacking.

### Notes

* The first successful declaration starts fixed windows: `2` hours for the attacker and `1` hour for the defender by default.
* Later successful starts consume slots without extending the window; a duration of `0` disables only the timed quota, not the concurrent-war chunk limit.

## 1.6.4

### Added

* Added a separate `/claimrules assembly` rule for Create and optional Sable contraption assembly.
* Added assembly status/toggle permissions and a dedicated Claim Rules GUI control.
* Added optional Sable `2.0.3` assembly protection compatibility.

### Changed

* Updated the mod version to `1.6.4`.
* Kept `/claimrules create` for Create and Offroad drills and saws only.
* Create and Sable assembly now validates all eight source AABB corners when a structure reaches claims; all corners must be peaceful claims of one side with assembly enabled.
* Fully unclaimed structures remain assemblable, while active contested-war chunks retain their previous behavior.
* Existing `create` settings and cooldowns are copied to the new `assembly` rule on first upgrade.
* Updated the optional claim-rules GUI protocol to version 2 and documented keybind reassignment through Minecraft controls.

## 1.6.3

Balance and compatibility update focused on limiting territory fight spam and clarifying the supported runtime range.

### Added

* Added daily outgoing territory fight limits for attacking sides.
* Added daily incoming territory fight limits for defending sides.
* Added configurable daily limit keys:

  * `war.daily_start_limit.max_started_chunks_per_attacker_side`
  * `war.daily_start_limit.max_accepted_chunks_per_defender_side`
* Added persistent per-day tracking for successful war starts.
* Added documentation for the new daily war-start limit configuration.

### Changed

* Updated the mod version to `1.6.3`.
* Updated metadata to keep Minecraft `1.21.1` as the tested baseline while allowing experimental same-jar probes up to Minecraft `1.27`.
* Updated metadata to keep NeoForge `21.1.232` as the tested baseline while allowing experimental probes through NeoForge `26.x`.
* Updated README and compatibility documentation to explain the baseline vs experimental support policy.
* Updated server/admin documentation to mention daily outgoing and incoming territory fight limits.

### Fixed

* Prevented one side from repeatedly starting too many territory fights in a single configured day.
* Prevented a defending side from being targeted by too many incoming territory fights in a single configured day.

### Notes

* By default, one attacking side can start up to `5` successful target chunk fights per configured day.
* By default, one defending side can receive up to `10` successful incoming target chunk fights per configured day.
* Setting either daily limit to `0` disables that specific limit.
* The daily reset uses the configured raid-window timezone.
* The fully tested baseline remains Minecraft `1.21.1` with NeoForge `21.1.232`; wider version metadata should be treated as experimental until smoke-tested.


## 1.6.2



Compatibility, release, and usability update focused on Xaero map integration, Aeronautics/Offroad bore protection, documentation, and the single-jar release workflow.



### Added



* Added Xaero World Map right-click support for starting wars at a selected chunk when the player has permission and the target is within the configured distance.

* Added configurable Xaero map war-start options: `xaero\_map\_war\_start.enabled` and `xaero\_map\_war\_start.max\_distance\_chunks`.

* Added Aeronautics/Offroad compatibility module for bore/multi-mining protection in protected claims.

* Added the `customclaims\_aeronautics` module to the bundled `opac-warfare` distribution jar.

* Added client keybind support for opening the claim rules GUI. The default key is `K` in the `CustomClaims` controls category.

* Added GitHub Actions build workflow that builds the project and uploads the `opac-warfare` jar artifact.

* Added public documentation for architecture, compatibility, configuration, development, and server administration.

* Added `AGENTS.md` with project notes for coding agents and future maintenance.



### Changed



* Improved Xaero temporary war waypoint handling with marker refresh tracking, stale waypoint cleanup, dimension filtering, and safer fallbacks when Xaero APIs are unavailable.

* Updated the README to focus on the public `Open Parties and Claims: Warfare` install flow and the single `opac-warfare` server artifact.

* Documented the one-jar compatibility policy and clarified optional integration behavior.

* Updated the project version to `1.6.2`.



### Fixed



* Improved claim rules GUI opening by adding a dedicated client-to-server payload instead of relying only on commands.

* Improved Aeronautics/Offroad bore protection safety with guarded reflection and one-time warning logs for unsupported API paths.

* Improved Xaero war-start error handling so the client receives a clear message when map war start is unavailable.



### Notes



* This release keeps optional integrations safe when the target mods are not installed.

* Server owners should install the generated `opac-warfare-1.6.2.jar` together with Open Parties and Claims.

* Recommended validation command: `./gradlew --no-daemon build :opac-warfare:jar`.



## 1.6.1

First public Modrinth release under the name **Open Parties and Claims: Warfare**.

### Added

* Open Parties and Claims territory integration.
* Chunk wars for party and personal claims.
* Contested chunks with capture progress.
* Raid windows and AFK defender checks.
* Claim protection rules for explosions, storage, villagers, and Withers.
* Create contraption and block-breaking protection.
* Create Big Cannons launch and terrain-damage protection.
* Optional Xaero temporary war waypoints.

### Notes

* This is an unofficial addon for Open Parties and Claims.
* This is the first public Modrinth release.
* Server owners should make backups before using it on production worlds.

