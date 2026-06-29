package dev.customclaims.protection.service;

import com.mojang.logging.LogUtils;
import dev.customclaims.core.api.model.PartyId;
import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.api.model.TerritoryStatus;
import dev.customclaims.core.service.DataStorageService;
import dev.customclaims.core.service.PartyService;
import dev.customclaims.core.service.TerritoryService;
import dev.customclaims.core.service.TerritoryStateService;
import java.util.ArrayList;
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

    private final TerritoryService territoryService;
    private final TerritoryStateService territoryStateService;
    private final PartyService partyService;
    private final DataStorageService dataStorageService;
    private final Map<String, Boolean> partyCreateMachinesEnabled = new ConcurrentHashMap<>();

    private MinecraftServer loadedServer;

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

    public boolean isPartyCreateMachinesEnabled(MinecraftServer server, PartyId partyId) {
        ensureLoaded(server);
        return partyCreateMachinesEnabled.getOrDefault(partyId.value(), false);
    }

    public boolean setPartyCreateMachinesEnabled(MinecraftServer server, PartyId partyId, boolean enabled) {
        try {
            ensureLoaded(server);
            partyCreateMachinesEnabled.put(partyId.value(), enabled);
            save(server);
            return true;
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to persist CustomClaims Create machine rule for {}", partyId, exception);
            return false;
        }
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

        Optional<PartyId> owner = territoryService.getClaimOwner(level, chunkPos);
        return owner.map(partyId -> isPartyCreateMachinesEnabled(level.getServer(), partyId)).orElse(true);
    }

    private boolean isContestedParticipant(ServerLevel level, ChunkPos chunkPos, UUID actorId) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(actorId);
        if (player == null) {
            return true;
        }

        ChunkPosKey key = ChunkPosKey.from(level, chunkPos);
        return partyService.getPlayerParty(player)
                .filter(partyId -> territoryStateService.isContestedParticipant(key, partyId))
                .isPresent();
    }

    private void ensureLoaded(MinecraftServer server) {
        if (loadedServer == server) {
            return;
        }

        synchronized (this) {
            if (loadedServer == server) {
                return;
            }

            partyCreateMachinesEnabled.clear();
            for (String line : dataStorageService.readLines(server, STORAGE_FILE)) {
                parseStorageLine(line).ifPresent(entry -> partyCreateMachinesEnabled.put(entry.partyId(), entry.enabled()));
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

        return Optional.of(new StoredCreateRule(parts[0].trim(), Boolean.parseBoolean(value)));
    }

    private void save(MinecraftServer server) {
        List<String> lines = new ArrayList<>();
        partyCreateMachinesEnabled.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .forEach(lines::add);
        dataStorageService.writeLines(server, STORAGE_FILE, lines);
    }

    private record StoredCreateRule(String partyId, boolean enabled) {
    }
}
