package dev.customclaims.protection.service;

import com.mojang.logging.LogUtils;
import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.api.model.ClaimSideId;
import dev.customclaims.core.api.model.TerritoryStatus;
import dev.customclaims.core.service.DataStorageService;
import dev.customclaims.core.service.PartyService;
import dev.customclaims.core.service.TerritoryService;
import dev.customclaims.core.service.TerritoryStateService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

public final class CreateMachinesProtectionService {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String STORAGE_FILE = "protection/create-machines.txt";
    private static final String ASSEMBLY_STORAGE_FILE = "protection/create-assemblies.txt";

    private final TerritoryService territoryService;
    private final TerritoryStateService territoryStateService;
    private final PartyService partyService;
    private final DataStorageService dataStorageService;
    private final Map<String, Boolean> createMachinesEnabled = new ConcurrentHashMap<>();
    private final Map<String, Boolean> assembliesEnabled = new ConcurrentHashMap<>();

    private MinecraftServer loadedServer;
    private boolean assemblyCooldownMigrationPending;

    public CreateMachinesProtectionService(
            TerritoryService territoryService,
            TerritoryStateService territoryStateService,
            PartyService partyService,
            DataStorageService dataStorageService
    ) {
        this.territoryService = territoryService;
        this.territoryStateService = territoryStateService;
        this.partyService = partyService;
        this.dataStorageService = dataStorageService;
    }

    public boolean isCreateMachinesEnabled(MinecraftServer server, ClaimSideId sideId) {
        ensureLoaded(server);
        return createMachinesEnabled.getOrDefault(sideId.storageKey(), false);
    }

    public boolean setCreateMachinesEnabled(MinecraftServer server, ClaimSideId sideId, boolean enabled) {
        try {
            ensureLoaded(server);
            createMachinesEnabled.put(sideId.storageKey(), enabled);
            save(server);
            return true;
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to persist CustomClaims Create machine rule for {}", sideId, exception);
            return false;
        }
    }

    public boolean isAssemblyEnabled(MinecraftServer server, ClaimSideId sideId) {
        ensureLoaded(server);
        return assembliesEnabled.getOrDefault(sideId.storageKey(), false);
    }

    public boolean setAssemblyEnabled(MinecraftServer server, ClaimSideId sideId, boolean enabled) {
        try {
            ensureLoaded(server);
            assembliesEnabled.put(sideId.storageKey(), enabled);
            saveAssemblies(server);
            return true;
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to persist CustomClaims contraption assembly rule for {}", sideId, exception);
            return false;
        }
    }

    /**
     * Returns whether the first assembly-rule load for this server copied legacy Create values.
     * The caller uses this one-shot signal to migrate the matching cooldown entries too.
     */
    public synchronized boolean consumeAssemblyCooldownMigration(MinecraftServer server) {
        ensureLoaded(server);
        if (!assemblyCooldownMigrationPending) {
            return false;
        }
        assemblyCooldownMigrationPending = false;
        return true;
    }

    public boolean canCreateAffect(ServerLevel level, BlockPos pos) {
        return canCreateAffect(level, pos, null);
    }

    public boolean canCreateAffect(ServerLevel level, BlockPos pos, UUID actorId) {
        ChunkPos chunkPos = new ChunkPos(pos);
        TerritoryStatus status = territoryService.getStatus(level, chunkPos);
        if (status == TerritoryStatus.UNCLAIMED) {
            return true;
        }
        if (status == TerritoryStatus.WAR_CONTESTED) {
            return actorId == null || isContestedParticipant(level, chunkPos, actorId);
        }

        Optional<ClaimSideId> owner = territoryService.getClaimOwnerSide(level, chunkPos);
        return owner.map(sideId -> isCreateMachinesEnabled(level.getServer(), sideId)).orElse(true);
    }

    public boolean canCreateAssembly(ServerLevel level, Iterable<BlockPos> sourcePositions) {
        List<BlockPos> positions = copyPositions(sourcePositions);
        return canCreateAssembly(level, positions, cornersOf(positions));
    }

    /**
     * Checks an assembly against its full source structure and its enclosing AABB corners.
     * Fully unclaimed structures retain the legacy allow behaviour. Once an assembly reaches a
     * claim, every AABB corner must be a peaceful claim of one side with assembly enabled.
     */
    public boolean canCreateAssembly(
            ServerLevel level,
            Iterable<BlockPos> sourcePositions,
            Iterable<BlockPos> boundsCorners
    ) {
        LinkedHashSet<BlockPos> positions = new LinkedHashSet<>(copyPositions(sourcePositions));
        List<BlockPos> corners = copyPositions(boundsCorners);
        positions.addAll(corners);
        if (positions.isEmpty()) {
            return true;
        }

        boolean touchesClaim = false;
        boolean touchesContestedWar = false;
        for (BlockPos pos : positions) {
            TerritoryStatus status = territoryService.getStatus(level, new ChunkPos(pos));
            if (status != TerritoryStatus.UNCLAIMED) {
                touchesClaim = true;
            }
            if (status == TerritoryStatus.WAR_CONTESTED) {
                touchesContestedWar = true;
            }
        }
        if (!touchesClaim) {
            return true;
        }

        // Active contested chunks keep the legacy Create rule and must not be evaluated through
        // their temporary fake owner.
        if (touchesContestedWar) {
            return positions.stream().allMatch(pos -> canCreateAffect(level, pos));
        }

        ClaimSideId assemblySide = null;
        for (BlockPos corner : corners) {
            ChunkPos chunkPos = new ChunkPos(corner);
            if (territoryService.getStatus(level, chunkPos) != TerritoryStatus.PEACEFUL_CLAIMED) {
                return false;
            }

            Optional<ClaimSideId> owner = territoryService.getClaimOwnerSide(level, chunkPos);
            if (owner.isEmpty()) {
                return false;
            }
            if (assemblySide == null) {
                assemblySide = owner.get();
            } else if (!assemblySide.equals(owner.get())) {
                return false;
            }
        }

        return assemblySide != null && isAssemblyEnabled(level.getServer(), assemblySide);
    }

    private boolean isContestedParticipant(ServerLevel level, ChunkPos chunkPos, UUID actorId) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(actorId);
        if (player == null) {
            return true;
        }

        ChunkPosKey key = ChunkPosKey.from(level, chunkPos);
        return territoryStateService.isContestedParticipant(key, partyService.getPlayerSide(player))
                || territoryStateService.isContestedParticipant(key, ClaimSideId.player(player.getUUID()));
    }

    private void ensureLoaded(MinecraftServer server) {
        if (loadedServer == server) {
            return;
        }

        synchronized (this) {
            if (loadedServer == server) {
                return;
            }

            createMachinesEnabled.clear();
            assembliesEnabled.clear();
            assemblyCooldownMigrationPending = false;
            for (String line : dataStorageService.readLines(server, STORAGE_FILE)) {
                parseStorageLine(line).ifPresent(entry -> createMachinesEnabled.put(entry.sideId(), entry.enabled()));
            }

            Path assemblyPath = dataStorageService.resolve(server, ASSEMBLY_STORAGE_FILE);
            if (Files.exists(assemblyPath)) {
                for (String line : dataStorageService.readLines(server, ASSEMBLY_STORAGE_FILE)) {
                    parseStorageLine(line).ifPresent(entry -> assembliesEnabled.put(entry.sideId(), entry.enabled()));
                }
            } else {
                assembliesEnabled.putAll(createMachinesEnabled);
                saveAssemblies(server);
                assemblyCooldownMigrationPending = true;
            }
            loadedServer = server;
        }
    }

    private Optional<StoredCreateRule> parseStorageLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return Optional.empty();
        }

        String[] parts = trimmed.split("=", 2);
        if (parts.length != 2 || parts[0].isBlank()) {
            LOGGER.warn("Ignoring malformed CustomClaims Create machine rule: {}", line);
            return Optional.empty();
        }

        String value = parts[1].trim();
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            LOGGER.warn("Ignoring CustomClaims Create machine rule with non-boolean value: {}", line);
            return Optional.empty();
        }

        try {
            return Optional.of(new StoredCreateRule(
                    ClaimSideId.parse(parts[0].trim()).storageKey(),
                    Boolean.parseBoolean(value)
            ));
        } catch (IllegalArgumentException exception) {
            LOGGER.warn("Ignoring CustomClaims Create machine rule with invalid side id: {}", line);
            return Optional.empty();
        }
    }

    private void save(MinecraftServer server) {
        List<String> lines = new ArrayList<>();
        createMachinesEnabled.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .forEach(lines::add);
        dataStorageService.writeLines(server, STORAGE_FILE, lines);
    }

    private void saveAssemblies(MinecraftServer server) {
        List<String> lines = new ArrayList<>();
        assembliesEnabled.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .forEach(lines::add);
        dataStorageService.writeLines(server, ASSEMBLY_STORAGE_FILE, lines);
    }

    private static List<BlockPos> copyPositions(Iterable<BlockPos> positions) {
        List<BlockPos> copied = new ArrayList<>();
        for (BlockPos pos : positions) {
            if (pos != null) {
                copied.add(pos.immutable());
            }
        }
        return copied;
    }

    private static List<BlockPos> cornersOf(List<BlockPos> positions) {
        if (positions.isEmpty()) {
            return List.of();
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : positions) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        return List.of(
                new BlockPos(minX, minY, minZ), new BlockPos(minX, minY, maxZ),
                new BlockPos(minX, maxY, minZ), new BlockPos(minX, maxY, maxZ),
                new BlockPos(maxX, minY, minZ), new BlockPos(maxX, minY, maxZ),
                new BlockPos(maxX, maxY, minZ), new BlockPos(maxX, maxY, maxZ)
        );
    }

    private record StoredCreateRule(String sideId, boolean enabled) {
    }
}