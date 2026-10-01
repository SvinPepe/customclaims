# Compatibility Policy

This project publishes one official server jar:

```text
opac-warfare/build/libs/opac-warfare-<version>.jar
```

## Baseline And Supported Range

The baseline for this branch is:

- Minecraft `26.1`
- NeoForge `26.1.2.112`
- Java `25`
- Open Parties and Claims `neoforge-26.3-0.31.6` or newer

The metadata range covers:

- Minecraft range: `[26.1,26.3)` (supports Minecraft `26.1` and `26.2`)
- NeoForge range: `[26.1.0,26.3)`

Key platform migrations in this branch:

- `net.minecraft.resources.ResourceLocation` migrated to `net.minecraft.resources.Identifier`.
- `FMLEnvironment.dist` field access migrated to `FMLEnvironment.getDist()`.
- `ResourceLocationArgument` migrated to `IdentifierArgument`.
- Java 25 runtime and compile baseline.

For Minecraft 1.21.x releases, use the separate 1.21.x release artifacts from the `main` branch.

Optional integrations are compiled against the `1.21.1` stack:

- Create `mc1.21.1-6.0.9` or newer
- Create Aeronautics/Offroad `1.3.0` or newer
- Sable `2.0.3` for optional assembly protection; it must satisfy its own Create dependency
- Create Big Cannons `5.11.7` or newer
- Xaero Minimap or Xaero World Map on compatible clients

## Unsupported Targets

The official jar is not promised to work on older `1.20.x`, Minecraft `26.x`,
Forge, Fabric, or Quilt targets.

Open Parties and Claims publishes many loader and Minecraft-version builds, but
this addon uses Minecraft, NeoForge, optional compat-mod, and mixin APIs that
are not binary-stable across that whole matrix. Broad metadata is a convenience
for nearby NeoForge probes, not a guarantee that Create, CBC, Aeronautics, Sable, or
Xaero internals still match on every candidate.

## Compatibility Probe

Use this workflow for any candidate 1.21.x version:

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
