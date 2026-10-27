package dev.customclaims.protection.service;

import com.mojang.logging.LogUtils;
import dev.customclaims.core.api.model.ClaimSideId;
import dev.customclaims.core.api.model.PartyId;
import dev.customclaims.core.api.model.TerritoryStatus;
import dev.customclaims.core.service.DataStorageService;
import dev.customclaims.core.service.TerritoryService;
import dev.customclaims.protection.compat.WarfareBridgeHolder;
import dev.customclaims.protection.config.ProtectionConfig;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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

    private final TerritoryService territoryService;
    private final OpenPartiesProtectionBypassService openPartiesProtectionBypassService;

    public ExplosionProtectionService(
            TerritoryService territoryService,
            OpenPartiesProtectionBypassService openPartiesProtectionBypassService
    ) {
        this.territoryService = territoryService;
        this.openPartiesProtectionBypassService = openPartiesProtectionBypassService;
    }

    public ExplosionProtectionService(
            TerritoryService territoryService,
            DataStorageService dataStorageService,
            OpenPartiesProtectionBypassService openPartiesProtectionBypassService
    ) {
        this(territoryService, openPartiesProtectionBypassService);
    }

    public boolean isPartyExplosionProtectionEnabled(MinecraftServer server, PartyId partyId) {
        return isExplosionProtectionEnabled(server, ClaimSideId.party(partyId));
    }

    public boolean isExplosionProtectionEnabled(MinecraftServer server, ClaimSideId sideId) {
        return readOpenPartiesExplosionProtection(server, sideId).orElse(true);
    }

    public boolean setPartyExplosionProtection(MinecraftServer server, PartyId partyId, boolean enabled) {
        return setExplosionProtection(server, ClaimSideId.party(partyId), enabled);
    }

    public boolean setExplosionProtection(MinecraftServer server, ClaimSideId sideId, boolean enabled) {
        try {
            boolean success = syncOpenPartiesExplosionProtection(server, sideId, enabled);
            if (!success) {
                LOGGER.warn("OPAC explosion protection sync for {} did not apply cleanly", sideId);
            }
            return success;
        } catch (RuntimeException exception) {
            LOGGER.warn("OPAC explosion protection sync failed for {}", sideId, exception);
            return false;
        }
    }

    public boolean canExplosionAffect(ServerLevel level, BlockPos pos) {
        return !isBlockProtectedFromExplosion(level, pos);
    }

    public void prepareExplosion(ServerLevel level, Explosion explosion, List<BlockPos> affectedBlocks) {
        Set<ChunkPos> affectedChunks = new HashSet<>();
        boolean hasContestedWarChunk = false;

        for (BlockPos pos : affectedBlocks) {
            ChunkPos chunkPos = new ChunkPos(pos);
            if (!affectedChunks.add(chunkPos)) {
                continue;
            }

            boolean isContested = WarfareBridgeHolder.isContestedWarChunk(level, chunkPos)
                    || territoryService.getStatus(level, chunkPos) == TerritoryStatus.WAR_CONTESTED;
            if (isContested) {
                hasContestedWarChunk = true;
            }
        }

        if (hasContestedWarChunk && explosion != null) {
            grantExplosionSourceBypass(level, explosion);
        }
    }

    public boolean isBlockProtectedFromExplosion(ServerLevel level, BlockPos pos) {
        if (!ProtectionConfig.CUSTOM_EXPLOSION_FILTER_ENABLED.get()) {
            return false;
        }

        ChunkPos chunkPos = new ChunkPos(pos);
        if (WarfareBridgeHolder.isContestedWarChunk(level, chunkPos) && ProtectionConfig.ALLOW_EXPLOSIONS_IN_WAR_CHUNKS.get()) {
            return false;
        }

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
}
