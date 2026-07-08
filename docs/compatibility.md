# Compatibility Policy

This project currently publishes one official server jar:

```text
opac-warfare/build/libs/opac-warfare-<version>.jar
```

That jar is anchored on the current full-compat stack:

- Minecraft `1.21.1`
- NeoForge `21.1.x`
- Java `21`
- Open Parties and Claims `neoforge-1.21.1-0.27.5` or newer for `1.21.1`

Optional integrations are preserved for this stack:

- Create `mc1.21.1-6.0.9` or newer
- Create Aeronautics/Offroad `1.3.0` or newer
- Create Big Cannons `5.11.7` or newer
- Xaero Minimap or Xaero World Map on compatible clients

## Unsupported Targets

The official jar is not promised to work on older `1.20.x`, newer `1.21.x`,
`26.x`, Forge, Fabric, or Quilt targets.

Open Parties and Claims publishes many loader and Minecraft-version builds, but
this addon uses Minecraft, NeoForge, optional compat-mod, and mixin APIs that
are not binary-stable across that whole matrix. One universal jar across every
OPaC-supported version would require heavy reflection and version shims, and
would risk breaking the full compat behavior that server owners expect.

## Compatibility Probe

Nearby NeoForge versions can be tested manually with the same built jar, but a
probe is not support until it passes boot and gameplay smoke tests.

Use this workflow for a candidate adjacent version:

1. Build the normal release jar:

   ```powershell
   .\gradlew.bat --no-daemon build :opac-warfare:jar
   ```

2. Install that same jar on the candidate server together with the matching OPaC
   build and any optional compat mods available for that Minecraft version.
3. Confirm the server boots without mixin, classloading, packet, or config
   errors.
4. Run the smoke checks from `docs/development.md`.
5. Only widen documented support or `minecraft_version_range` after those checks
   pass.

If a candidate fails because of Minecraft, NeoForge, OPaC, or compat-mod API
drift, leave it unsupported instead of adding broad reflection-only fixes.
