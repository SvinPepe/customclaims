# Multi-Loader and Multi-Version Architecture Foundation

This document defines the architectural specification and implementation roadmap for supporting **Multi-Loader (Fabric + NeoForge via Architectury)** and **Multi-Version (Minecraft 1.21.1 and 26.1 via Stonecutter)** in **Open Parties and Claims: Warfare**.

---

## 1. Architectural Goals

1. **Loader Agnosticism**: Keep core warfare logic (war manager, capture progress formulas, raid windows, sector wedges, AFK tracking, scoreboards, and configs) decoupled from modloader-specific APIs through service interfaces.
2. **Multi-Loader Support (Architectury Loom)**:
   - **Fabric**: Support Fabric Loader using Fabric API, Fabric Networking, and Fabric Permissions API (with LuckPerms bridge).
   - **NeoForge**: Support NeoForge using NeoForge EventBus, CustomPacketPayload network registrar, and NeoForge PermissionAPI.
3. **Multi-Version Management (Stonecutter)**:
   - Manage cross-version differences between Minecraft `1.21.1` and `26.1` using Stonecutter preprocessor comments (`//? if >=1.21.2 { ... }`) and versioned subproject branches without code duplication.

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
├── settings.gradle            # Multi-project inclusions & Stonecutter project tree declaration
├── stonecutter.gradle         # Stonecutter controller script (active target & parameters)
├── gradle.properties          # Version matrices
│
├── common/                    # Loader-agnostic Warfare domain logic
│   ├── src/main/java/         # Shared domain models, algorithms, config keys, platform interfaces
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

The loader abstraction layer in `dev.customclaims.core.platform` decouples domain services from modloader-specific classes:

### 4.1 Platform Environment (`IPlatformHelper`)
Loader-agnostic query for mod presence, development mode, and config path:
```java
public interface IPlatformHelper {
    String getPlatformName();
    boolean isModLoaded(String modId);
    boolean isDevelopmentEnvironment();
    Path getConfigDirectory();
}
```

### 4.2 Permissions (`IPermissionHelper`)
Loader-agnostic permission queries:
- **NeoForge**: Resolved via `PermissionAPI.getPermission(player, node)` using `CustomClaimsPermissions` nodes.
- **Fabric**: Resolved via Fabric Permissions API / LuckPerms string query.
```java
public interface IPermissionHelper {
    boolean hasPermission(ServerPlayer player, String permissionNode);
}
```
Domain services like `PermissionService` query `PlatformHelperHolder.getPermissions()`.

### 4.3 Networking (`INetworkHelper`)
Loader-agnostic packet dispatch:
- **NeoForge**: Dispatches via `PacketDistributor.sendToPlayer` and `PacketDistributor.sendToServer`, checking client channel readiness via `NetworkRegistry.hasChannel`.
- **Fabric**: Dispatches via Fabric Networking `ServerPlayNetworking.send` and `ClientPlayNetworking.send`.
```java
public interface INetworkHelper {
    boolean canSendToPlayer(ServerPlayer player, ResourceLocation channelId);
    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
    void sendToServer(CustomPacketPayload payload);
}
```
Client and server handlers (e.g. `WarMarkerSyncHandler`, `XaeroMapWarStartOption`) interact exclusively with `PlatformHelperHolder.getNetwork()`.

### 4.4 Service Resolution (`PlatformHelperHolder`)
Uses Java `ServiceLoader` with fallback to `NeoForge` implementations when running on NeoForge.

---

## 5. Stonecutter Multi-Version Configuration

Stonecutter allows targeting both `1.21.1` and `26.1` with single source files containing preprocessor directives:

### 5.1 Project Tree Declaration in `settings.gradle`
```groovy
stonecutter {
    kotlinController = false
    centralScript = "build.gradle"
    create(rootProject) {
        branch("common") { versions "1.21.1", "26.1" }
        branch("fabric") { versions "1.21.1", "26.1" }
        branch("neoforge") { versions "1.21.1", "26.1" }
    }
}
```

### 5.2 Controller Script in `stonecutter.gradle`
```groovy
plugins {
    id 'dev.kikugie.stonecutter'
}

stonecutter.active "1.21.1"

stonecutter.parameters {
    // version-specific preprocessor tokens
}
```

### 5.3 NBT API Differences Example (`1.21.1` vs `26.1`)
```java
//? if <26.0 {
CompoundTag tag = nbt.getCompound("WarData");
long timer = tag.getLong("ExpiresAt");
//?} else {
/*CompoundTag tag = nbt.getCompoundOrEmpty("WarData");
long timer = tag.getLong("ExpiresAt").orElse(0L);
*///?}
```

### 5.4 Active Version Switching
- **In IDE**: Using the Stonecutter IntelliJ IDEA plugin switcher widget in the status bar or the `Stonecutter` tool window.
- **In Configuration**: Changing `stonecutter.active "1.21.1"` to `stonecutter.active "26.1"` in `stonecutter.gradle` and re-syncing Gradle.

---

## 6. Migration Roadmap

1. **Step 1 (Completed)**: Decouple `opac-better-protection` into its own standalone repository; isolate `opac-warfare` in this repository and verify clean compilation & tests.
2. **Step 2 (Completed)**: Correct `stonecutter.gradle` configuration and technical architecture specification.
3. **Step 3 (Completed)**: Implement loader abstraction interfaces (`IPlatformHelper`, `IPermissionHelper`, `INetworkHelper`, `PlatformHelperHolder`) and rewire network and permission callers to remove loader-specific calls from domain handlers.
4. **Step 4**: Introduce physical `common`, `fabric`, and `neoforge` branches under Stonecutter, adding `fabric.mod.json`, Fabric networking, and Fabric lifecycle events.
5. **Step 5**: Enable Stonecutter active switching between `1.21.1` and `26.1` targets and configure CI matrix builds.
