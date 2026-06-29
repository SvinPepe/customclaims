package dev.customclaims.protection.service;

import com.mojang.logging.LogUtils;
import dev.customclaims.core.api.model.ClaimSideId;
import dev.customclaims.core.api.model.PartyId;
import dev.customclaims.core.api.model.TerritoryStatus;
import dev.customclaims.core.service.DataStorageService;
import dev.customclaims.core.service.TerritoryService;
import dev.customclaims.protection.config.ProtectionConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import org.slf4j.Logger;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.parties.party.member.api.IPartyMemberAPI;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.parties.party.api.IServerPartyAPI;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI;
import xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions;

public final class ExplosionProtectionService {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String STORAGE_FILE = "protection/explosion-protection.txt";

    private final TerritoryService territoryService;
    private final DataStorageService dataStorageService;
    private final OpenPartiesProtectionBypassService openPartiesProtectionBypassService;
    private final Map<String, Boolean> explosionProtection = new ConcurrentHashMap<>();

    private MinecraftServer loadedServer;

    public ExplosionProtectionService(
            TerritoryService territoryService,
            DataStorageService dataStorageService,
            OpenPartiesProtectionBypassService openPartiesProtectionBypassService
    ) {
        this.territoryService = territoryService;
        this.dataStorageService = dataStorageService;
        this.openPartiesProtectionBypassService = openPartiesProtectionBypassService;
    }

    public boolean isPartyExplosionProtectionEnabled(MinecraftServer server, PartyId partyId) {
        return isExplosionProtectionEnabled(server, ClaimSideId.party(partyId));
    }

    public boolean isExplosionProtectionEnabled(MinecraftServer server, ClaimSideId sideId) {
        ensureLoaded(server);
        Boolean local = explosionProtection.get(sideId.storageKey());
        if (local != null) {
            return local;
        }
        return readOpenPartiesExplosionProtection(server, sideId).orElse(true);
    }

    public boolean setPartyExplosionProtection(MinecraftServer server, PartyId partyId, boolean enabled) {
        return setExplosionProtection(server, ClaimSideId.party(partyId), enabled);
    }

    public boolean setExplosionProtection(MinecraftServer server, ClaimSideId sideId, boolean enabled) {
        try {
            ensureLoaded(server);
            explosionProtection.put(sideId.storageKey(), enabled);
            save(server);
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to persist CustomClaims explosion protection rule for {}", sideId, exception);
            return false;
        }

        try {
            if (!syncOpenPartiesExplosionProtection(server, sideId, enabled)) {
                LOGGER.warn("CustomClaims explosion rule for {} was saved, but OPC sync did not apply cleanly", sideId);
            }
        } catch (RuntimeException exception) {
            LOGGER.warn("CustomClaims explosion rule for {} was saved, but OPC sync failed", sideId, exception);
        }

        return true;
    }

    public boolean canExplosionAffect(ServerLevel level, BlockPos pos) {
        return !isBlockProtectedFromExplosion(level, pos);
    }

    public void prepareExplosion(ServerLevel level, Explosion explosion, List<BlockPos> affectedBlocks) {
        Set<ChunkPos> affectedChunks = new HashSet<>();
        boolean sourceNeedsBypass = false;

        for (BlockPos pos : affectedBlocks) {
            ChunkPos chunkPos = new ChunkPos(pos);
            if (!affectedChunks.add(chunkPos)) {
                continue;
            }

            boolean explosionsAllowed = canExplosionAffect(level, pos);
            if (territoryService.getStatus(level, chunkPos) != TerritoryStatus.UNCLAIMED) {
                syncOpenPartiesClaimExplosionException(level, chunkPos, explosionsAllowed);
                sourceNeedsBypass |= explosionsAllowed;
            }
        }

        if (sourceNeedsBypass && explosion != null) {
            grantExplosionSourceBypass(level, explosion);
        }
    }

    public boolean isBlockProtectedFromExplosion(ServerLevel level, BlockPos pos) {
        if (!ProtectionConfig.CUSTOM_EXPLOSION_FILTER_ENABLED.get()) {
            return false;
        }

        ChunkPos chunkPos = new ChunkPos(pos);
        TerritoryStatus status = territoryService.getStatus(level, chunkPos);
        if (status == TerritoryStatus.UNCLAIMED) {
            return false;
        }
        if (status == TerritoryStatus.WAR_CONTESTED && ProtectionConfig.ALLOW_EXPLOSIONS_IN_WAR_CHUNKS.get()) {
            return false;
        }

        Optional<ClaimSideId> owner = territoryService.getClaimOwnerSide(level, chunkPos);
        return owner.filter(sideId -> isExplosionProtectionEnabled(level.getServer(), sideId)).isPresent();
    }

    private void ensureLoaded(MinecraftServer server) {
        if (loadedServer == server) {
            return;
        }

        synchronized (this) {
            if (loadedServer == server) {
                return;
            }

            explosionProtection.clear();
            for (String line : dataStorageService.readLines(server, STORAGE_FILE)) {
                parseStorageLine(line).ifPresent(entry -> explosionProtection.put(entry.sideId(), entry.enabled()));
            }
            loadedServer = server;
        }
    }

    private Optional<StoredExplosionRule> parseStorageLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return Optional.empty();
        }

        String[] parts = trimmed.split("=", 2);
        if (parts.length != 2 || parts[0].isBlank()) {
            LOGGER.warn("Ignoring malformed CustomClaims explosion protection rule: {}", line);
            return Optional.empty();
        }

        String value = parts[1].trim();
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            LOGGER.warn("Ignoring CustomClaims explosion protection rule with non-boolean value: {}", line);
            return Optional.empty();
        }

        try {
            return Optional.of(new StoredExplosionRule(
                    ClaimSideId.parse(parts[0].trim()).storageKey(),
                    Boolean.parseBoolean(value)
            ));
        } catch (IllegalArgumentException exception) {
            LOGGER.warn("Ignoring CustomClaims explosion protection rule with invalid side id: {}", line);
            return Optional.empty();
        }
    }

    private void save(MinecraftServer server) {
        List<String> lines = new ArrayList<>();
        explosionProtection.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .forEach(lines::add);
        dataStorageService.writeLines(server, STORAGE_FILE, lines);
    }

    private Optional<Boolean> readOpenPartiesExplosionProtection(MinecraftServer server, ClaimSideId sideId) {
        if (sideId.isPlayer()) {
            Optional<UUID> playerUuid = sideId.playerUuid();
            if (playerUuid.isEmpty()) {
                return Optional.empty();
            }

            IPlayerConfigAPI config = OpenPACServerAPI.get(server)
                    .getPlayerConfigManager()
                    .getLoadedConfig(playerUuid.get());
            if (config == null) {
                return Optional.empty();
            }

            boolean explosionsAllowed = Boolean.TRUE.equals(
                    effectiveClaimConfig(config).getEffective(PlayerConfigOptions.CLAIM_EXCEPTION_BLOCKS_BY_EXPLOSIONS)
            );
            return Optional.of(!explosionsAllowed);
        }

        Optional<UUID> partyUuid = sideId.partyId().flatMap(this::parsePartyUuid);
        if (partyUuid.isEmpty()) {
            return Optional.empty();
        }

        OpenPACServerAPI api = OpenPACServerAPI.get(server);
        IServerPartyAPI party = api.getPartyManager().getPartyById(partyUuid.get());
        if (party == null) {
            return Optional.empty();
        }

        IPlayerConfigAPI partyConfig = api.getPlayerConfigManager().getPartyOwnerConfig(party.getOwner().getUUID());
        if (partyConfig == null) {
            return Optional.empty();
        }

        boolean explosionsAllowed = Boolean.TRUE.equals(
                effectiveClaimConfig(partyConfig).getEffective(PlayerConfigOptions.CLAIM_EXCEPTION_BLOCKS_BY_EXPLOSIONS)
        );
        return Optional.of(!explosionsAllowed);
    }

    private boolean syncOpenPartiesExplosionProtection(MinecraftServer server, ClaimSideId sideId, boolean enabled) {
        if (sideId.isPlayer()) {
            Optional<UUID> playerUuid = sideId.playerUuid();
            if (playerUuid.isEmpty()) {
                return false;
            }

            IPlayerConfigAPI config = OpenPACServerAPI.get(server)
                    .getPlayerConfigManager()
                    .getLoadedConfig(playerUuid.get());
            return config != null && setExplosionExceptionOnConfigAndSubConfigs(config, !enabled);
        }

        Optional<UUID> partyUuid = sideId.partyId().flatMap(this::parsePartyUuid);
        if (partyUuid.isEmpty()) {
            return false;
        }

        OpenPACServerAPI api = OpenPACServerAPI.get(server);
        IServerPartyAPI party = api.getPartyManager().getPartyById(partyUuid.get());
        if (party == null) {
            return false;
        }

        boolean explosionsAllowed = !enabled;
        boolean applied = false;
        boolean success = true;

        IPlayerConfigAPI partyConfig = api.getPlayerConfigManager().getPartyOwnerConfig(party.getOwner().getUUID());
        if (partyConfig != null) {
            applied = true;
            success &= setExplosionExceptionOnConfigAndSubConfigs(partyConfig, explosionsAllowed);
        }

        Set<UUID> playerConfigIds = new LinkedHashSet<>();
        playerConfigIds.add(party.getOwner().getUUID());
        party.getMemberInfoStream().map(IPartyMemberAPI::getUUID).forEach(playerConfigIds::add);

        for (UUID memberId : playerConfigIds) {
            IPlayerConfigAPI memberConfig = api.getPlayerConfigManager().getLoadedConfig(memberId);
            if (memberConfig != null) {
                applied = true;
                success &= setExplosionExceptionOnConfigAndSubConfigs(memberConfig, explosionsAllowed);
            }
        }

        return applied && success;
    }

    private boolean syncOpenPartiesClaimExplosionException(
            ServerLevel level,
            ChunkPos chunkPos,
            boolean explosionsAllowed
    ) {
        OpenPACServerAPI api = OpenPACServerAPI.get(level.getServer());
        IPlayerChunkClaimAPI claim = api.getServerClaimsManager().get(level.dimension().location(), chunkPos);
        if (claim == null) {
            return false;
        }
        IPlayerConfigAPI config = api.getChunkProtection().getConfig(claim);
        return setExplosionException(config, explosionsAllowed);
    }

    private void grantExplosionSourceBypass(ServerLevel level, Explosion explosion) {
        LivingEntity indirectSource = explosion.getIndirectSourceEntity();
        if (indirectSource != null) {
            openPartiesProtectionBypassService.grantUntilNextServerTick(level, indirectSource);
        }

        Entity directSource = explosion.getDirectSourceEntity();
        if (directSource != null) {
            openPartiesProtectionBypassService.grantUntilNextServerTick(level, directSource);
        }
    }

    private IPlayerConfigAPI effectiveClaimConfig(IPlayerConfigAPI config) {
        IPlayerConfigAPI usedSubConfig = config.getUsedSubConfig();
        return usedSubConfig == null ? config : usedSubConfig;
    }

    private boolean setExplosionExceptionOnConfigAndSubConfigs(IPlayerConfigAPI config, boolean explosionsAllowed) {
        boolean success = setExplosionException(config, explosionsAllowed);
        for (IPlayerConfigAPI subConfig : config.getSubConfigAPIStream().toList()) {
            success &= setExplosionException(subConfig, explosionsAllowed);
        }
        return success;
    }

    private boolean setExplosionException(IPlayerConfigAPI config, boolean explosionsAllowed) {
        if (config == null) {
            return false;
        }

        IPlayerConfigAPI.SetResult blockResult = config.tryToSet(
                PlayerConfigOptions.CLAIM_EXCEPTION_BLOCKS_BY_EXPLOSIONS,
                explosionsAllowed
        );
        IPlayerConfigAPI.SetResult entityResult = config.tryToSet(
                PlayerConfigOptions.CLAIM_EXCEPTION_ENTITIES_BY_EXPLOSIONS,
                explosionsAllowed
        );
        return isSuccessfulSet(blockResult) && isSuccessfulSet(entityResult);
    }

    private boolean isSuccessfulSet(IPlayerConfigAPI.SetResult result) {
        return result == IPlayerConfigAPI.SetResult.SUCCESS
                || result == IPlayerConfigAPI.SetResult.DEFAULTED;
    }

    private Optional<UUID> parsePartyUuid(PartyId partyId) {
        try {
            return Optional.of(UUID.fromString(partyId.value()));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private record StoredExplosionRule(String sideId, boolean enabled) {
    }
}
