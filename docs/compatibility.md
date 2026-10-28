# Compatibility Policy

This project publishes one official server distribution jar:

```text
opac-warfare/build/libs/opac-warfare-<version>.jar
```

## Compatibility Matrix

| Mod / Component | Supported Version / Range | Status | Notes |
| --- | --- | --- | --- |
| **Minecraft** | `1.21.1` | **Verified Baseline** | Compiled with Java 21. |
| **Minecraft (Probes)** | `[1.21.1, 26.0)` | Experimental | Supports experimental testing across 1.21.x (up to 1.21.11). |
| **NeoForge** | `21.1.232` | **Verified Baseline** | Full regression and smoke test baseline. |
| **NeoForge (Probes)** | `[21.1.232, 26.0)` | Experimental | Metadata allows candidate NeoForge builds. |
| **Open Parties and Claims (OPaC)** | `neoforge-1.21.1-0.27.5`+ | **Required Baseline** | Authoritative source for claims and party data. |
| **Xaero's Minimap** | Compatible `1.21.1` builds | Optional Client | Fair-play temporary war waypoints sync. |
| **Xaero's World Map** | `1.21.1-1.41.2` or newer | Optional Client | Temporary war waypoints and right-click "Start war here" menu item. |
| **OPaC: Better Protection** | `2.0.0`+ | Optional Companion Mod | Companion protection mod (Create, CBC, explosions, `/claimrules`). Integrates via `WarfareBridge`. |

## Multi-Loader and Multi-Version Roadmap

- **Fabric Support**: Loader abstraction interfaces (`IPlatformHelper`, `IPermissionHelper`, `INetworkHelper`, `PlatformHelperHolder`) are prepared for Fabric Loader integration via Architectury Loom.
- **Stonecutter Multi-Version**: The repository includes `stonecutter.gradle` for synchronized compilation targeting Minecraft `1.21.1` and modern versions like `26.1`. See [Multi-Loader & Multi-Version Architecture](multiversion-multiloader.md) for full details.

## Baseline And Experimental Range

The full tested baseline remains:

- Minecraft `1.21.1`
- NeoForge `21.1.232`
- Java `21`
- Open Parties and Claims `neoforge-1.21.1-0.27.5` or newer for `1.21.1`

The jar metadata is intentionally wider for experimental same-jar probes:

- Minecraft range: `[1.21.1,26.0)`
- NeoForge range: `[21.1.232,26.0)`

That means newer Minecraft 1.21.x and NeoForge 21.x/25.x builds can try the same
jar, but only the baseline is considered verified until a server boot and gameplay
smoke test pass.

**Minecraft 26.x is not supported by this jar.** Minecraft 26.1 introduced
breaking API changes incompatible with this 1.21.x build:

- `net.minecraft.resources.ResourceLocation` was renamed to `net.minecraft.resources.Identifier`.
- `FMLEnvironment.dist` (static field) was removed in favour of `FMLEnvironment.getDist()`.
- Java 25 is now required (this jar targets Java 21).

A separate port for 26.x will be released as a distinct artifact.

## Companion Mod Integration: Better Protection

Starting in Warfare 2.0, claim protection rules and mechanics (Create contraption assembly, drills, saws, CBC/Clockwork cannon projectile impacts, explosion protection, and `/claimrules`) have been extracted into the companion mod:
👉 **[Open Parties and Claims: Better Protection](https://github.com/SvinPepe/opac-better-protection)**

`opac-warfare` and `opac-better-protection` integrate dynamically through `dev.customclaims.api.war.WarfareBridgeHolder`. Neither mod has a hard runtime dependency on the other:
- If both mods are installed, `opac-better-protection` automatically allows block damage and contraption interaction in contested war chunks while preserving claim protection elsewhere.
- If only `opac-warfare` is installed, warfare runs without custom protection overrides.

## Unsupported Targets

The official jar is not promised to work on older `1.20.x`, Minecraft `26.x`,
Forge, Fabric, or Quilt targets.

Open Parties and Claims publishes many loader and Minecraft-version builds, but
this addon uses Minecraft, NeoForge, optional compat-mod, and mixin APIs that
are not binary-stable across that whole matrix. Broad metadata is a convenience
for nearby NeoForge probes, not a guarantee that Xaero internals still match on every candidate.

## Compatibility Probe

Use this workflow for any candidate 1.21.x version:

1. Build the normal release jar:

   ```powershell
   .\gradlew.bat --no-daemon :opac-warfare:jar
   ```

2. Install that same jar on the candidate server together with the matching OPaC
   build and any optional compat mods available for that Minecraft version.
3. Confirm the server boots without mixin, classloading, packet, or config
   errors.
4. Run the smoke checks from [Development Guide](development.md).
5. Document the candidate as verified only after those checks pass.

If a candidate fails because of Minecraft, NeoForge, OPaC, or compat-mod API
drift, document it as unsupported even though the broad experimental metadata
allows the loader to try the jar.

