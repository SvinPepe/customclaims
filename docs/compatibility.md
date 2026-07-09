# Compatibility Policy

This project publishes one official server jar:

```text
opac-warfare/build/libs/opac-warfare-<version>.jar
```

## Baseline And Experimental Range

The full tested baseline remains:

- Minecraft `1.21.1`
- NeoForge `21.1.232`
- Java `21`
- Open Parties and Claims `neoforge-1.21.1-0.27.5` or newer for `1.21.1`

The jar metadata is intentionally wider for experimental same-jar probes:

- Minecraft range: `[1.21.1,1.27)`
- NeoForge range: `[21.1.232,27.0)`

That means NeoForge `21.x` through `26.x` can try the same jar, but only the
baseline is considered verified until a server boot and gameplay smoke test pass.

Optional integrations are compiled against the `1.21.1` stack:

- Create `mc1.21.1-6.0.9` or newer
- Create Aeronautics/Offroad `1.3.0` or newer
- Create Big Cannons `5.11.7` or newer
- Xaero Minimap or Xaero World Map on compatible clients

## Unsupported Targets

The official jar is not promised to work on older `1.20.x`, Minecraft `1.27+`,
Forge, Fabric, or Quilt targets.

Open Parties and Claims publishes many loader and Minecraft-version builds, but
this addon uses Minecraft, NeoForge, optional compat-mod, and mixin APIs that
are not binary-stable across that whole matrix. Broad metadata is a convenience
for nearby NeoForge probes, not a guarantee that Create, CBC, Aeronautics, or
Xaero internals still match on every candidate.

## Compatibility Probe

Use this workflow for any candidate NeoForge `21.x` through `26.x` version:

1. Build the normal release jar:

   ```powershell
   .\gradlew.bat --no-daemon build :opac-warfare:jar
   ```

2. Install that same jar on the candidate server together with the matching OPaC
   build and any optional compat mods available for that Minecraft version.
3. Confirm the server boots without mixin, classloading, packet, or config
   errors.
4. Run the smoke checks from `docs/development.md`.
5. Document the candidate as verified only after those checks pass.

If a candidate fails because of Minecraft, NeoForge, OPaC, or compat-mod API
drift, document it as unsupported even though the broad experimental metadata
allows the loader to try the jar.