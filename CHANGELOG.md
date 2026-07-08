# Changelog

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

