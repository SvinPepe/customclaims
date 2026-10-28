# Multi-Loader and Multi-Version Architecture Foundation

This document defines the architectural specification and implementation roadmap for supporting **Multi-Loader (Fabric + NeoForge via Architectury)** and **Multi-Version (Minecraft 1.21.1 and 26.1 via Stonecutter)** in **Open Parties and Claims: Warfare**.

---

## 1. Architectural Goals

1. **Loader Agnosticism**: Extract core warfare logic (war manager, capture progress formulas, raid windows, sector wedges, AFK tracking, scoreboards, and configs) into a common module that has zero dependencies on modloader-specific APIs.
2. **Multi-Loader Support (Architectury Loom)**:
   - **Fabric**: Support Fabric Loader using Fabric API, Fabric Networking, and Fabric Permissions API (with LuckPerms bridge).
   - **NeoForge**: Support NeoForge using NeoForge EventBus, CustomPacketPayload network registrar, and NeoForge PermissionAPI.
3. **Multi-Version Management (Stonecutter)**:
   - Manage cross-version differences between Minecraft `1.21.1` and `26.1` using Stonecutter preprocessor comments (`//? if >=1.21.2 { ... }`) and versioned subproject controllers without code duplication.

---

## 2. Target Matrix

| Dimension | Target A (Baseline) | Target B (Modern Port) |
|---|---|---|
| **Minecraft Version** | `1.21.1` | `26.1` |
| **Java Version** | Java `21` | Java `25` |
| **NeoForge Version** | `21.1.232`+ | `26.1.0`+ |
| **Fabric Loader** | `0.16.x` | `0.17.x` |
| **OPaC Dependency** | `neoforge-1.21.1-0.27.5` / Fabric equivalent | `26.1` compatible build |

---

## 3. Project Structure

```text
opac-warfare/
├── build.gradle               # Central multi-project build script
├── settings.gradle            # Multi-project inclusions and Stonecutter controller
├── stonecutter.gradle         # Stonecutter version definitions and active target
├── gradle.properties          # Version matrices
│
├── common/                    # Loader-agnostic Warfare domain logic
│   ├── src/main/java/         # Shared domain models, algorithms, config keys
│   └── src/main/resources/    # Shared assets (lang, textures, icons)
│
├── fabric/                    # Fabric Loader target
│   ├── src/main/java/         # Fabric ModInitializer, Fabric packet handlers, Fabric mixins
│   └── src/main/resources/    # fabric.mod.json, customclaims_warfare.fabric.mixins.json
│
├── neoforge/                  # NeoForge Loader target
│   ├── src/main/java/         # NeoForge @Mod, EventBus listeners, NeoForge network registration
│   └── src/main/resources/    # META-INF/neoforge.mods.toml
│
└── docs/                      # Documentation
    ├── architecture.md
    ├── development.md
    └── multiversion-multiloader.md
```

---

## 4. Loader Abstraction Boundaries

### 4.1 Networking
- **NeoForge**: Implements `CustomPacketPayload` and registers payloads via `RegisterPayloadHandlersEvent`.
- **Fabric**: Implements `CustomPayload` and registers via `PayloadTypeRegistry.playC2S()` / `playS2C()`.
- **Common API**: Define packet DTOs (`WarMarkerDto`, `CaptureProgressDto`, `WarHudDto`) in `common`, and dispatch through an abstraction interface:
  ```java
  public interface INetworkChannel {
      void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
      void sendToTracking(ServerLevel level, ChunkPos pos, CustomPacketPayload payload);
      void sendToAll(MinecraftServer server, CustomPacketPayload payload);
  }
  ```

### 4.2 Permissions
- **NeoForge**: `PermissionAPI.getPermission(player, node)`
- **Fabric**: `Permissions.check(player, node.getNodeName(), defaultVal)` (via Fabric Permissions API).
- **Common API**: `PermissionService` delegates to a loader-provided permission resolver.

### 4.3 Events & Lifecycle
- **NeoForge**: Registered on `NeoForge.EVENT_BUS` using `@SubscribeEvent` (`ServerTickEvent`, `PlayerInteractEvent`, `LivingDeathEvent`).
- **Fabric**: Registered via Fabric API event callbacks (`ServerTickEvents.END_SERVER_TICK`, `UseBlockCallback.EVENT`, `ServerLivingEntityEvents.AFTER_DEATH`).

---

## 5. Stonecutter Multi-Version Configuration

Stonecutter allows targeting both `1.21.1` and `26.1` with single source files containing preprocessor directives:

### 5.1 NBT API Differences (`1.21.1` vs `26.1`)
```java
//? if <26.0 {
CompoundTag tag = nbt.getCompound("WarData");
long timer = tag.getLong("ExpiresAt");
//?} else {
/*CompoundTag tag = nbt.getCompoundOrEmpty("WarData");
long timer = tag.getLong("ExpiresAt").orElse(0L);
*///?}
```

### 5.2 Client Rendering / GuiGraphics (`1.21.1` vs `26.1`)
```java
//? if <26.0 {
guiGraphics.drawString(font, title, x, y, 0xFFFFFF);
//?} else {
/*net.minecraft.client.gui.GuiGraphicsExtractor.drawString(guiGraphics, font, title, x, y, 0xFFFFFF);
*///?}
```

### 5.3 Switching and Building with Stonecutter
- To switch active development version in IntelliJ IDEA:
  ```powershell
  .\gradlew.bat SetActiveVersion --version 1.21.1
  # or
  .\gradlew.bat SetActiveVersion --version 26.1
  ```
- To compile all configured versions and loaders concurrently:
  ```powershell
  .\gradlew.bat chiseledBuild
  ```

---

## 6. Migration Roadmap

1. **Step 1 (Completed)**: Decouple `opac-better-protection` into its own standalone repository; isolate `opac-warfare` in this repository and verify clean compilation & tests.
2. **Step 2 (Current)**: Lay out the `stonecutter.gradle` configuration and technical architecture specification.
3. **Step 3 (Next)**: Introduce the `common` subproject, migrate loader-agnostic classes from `customclaims-core` and `customclaims-war`, and establish the loader abstraction interfaces (`IPlatformHelper`, `INetworkHelper`, `IPermissionHelper`).
4. **Step 4**: Introduce the `fabric` subproject with `fabric.mod.json`, Fabric networking, and Fabric lifecycle events.
5. **Step 5**: Enable Stonecutter active switching between `1.21.1` and `26.1` targets and configure CI matrix builds.
