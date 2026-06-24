package dev.customclaims.protection.service;

import dev.customclaims.core.api.model.PartyId;
import dev.customclaims.core.api.model.TerritoryStatus;
import dev.customclaims.core.service.TerritoryService;
import dev.customclaims.protection.config.ProtectionConfig;
import java.util.HashSet;
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
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.parties.party.member.api.IPartyMemberAPI;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.parties.party.api.IServerPartyAPI;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI;
import xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions;

public final class ExplosionProtectionService {
    private final TerritoryService territoryService;
    private final OpenPartiesProtectionBypassService openPartiesProtectionBypassService;
    private final Map<PartyId, Boolean> partyExplosionProtection = new ConcurrentHashMap<>();

    public ExplosionProtectionService(
            TerritoryService territoryService,
            OpenPartiesProtectionBypassService openPartiesProtectionBypassService
    ) {
        this.territoryService = territoryService;
        this.openPartiesProtectionBypassService = openPartiesProtectionBypassService;
    }

    public void prepareExplosion(ServerLevel level, Explosion explosion, List<BlockPos> affectedBlocks) {
        Set<ChunkPos> affectedChunks = new HashSet<>();
        boolean shouldBypassOpenParties = false;
        for (BlockPos pos : affectedBlocks) {
            ChunkPos chunkPos = new ChunkPos(pos);
            if (affectedChunks.add(chunkPos) && shouldBypassOpenPartiesExplosionProtection(level, chunkPos)) {
                shouldBypassOpenParties = true;
            }
        }

        if (!shouldBypassOpenParties) {
            return;
        }

        LivingEntity indirectSource = explosion.getIndirectSourceEntity();
        if (indirectSource != null) {
            openPartiesProtectionBypassService.grantUntilNextServerTick(level, indirectSource);
        }

        Entity directSource = explosion.getDirectSourceEntity();
        if (directSource != null) {
            openPartiesProtectionBypassService.grantUntilNextServerTick(level, directSource);
        }
    }

    public boolean canExplosionAffect(ServerLevel level, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        TerritoryStatus status = territoryService.getStatus(level, chunkPos);
        if (status == TerritoryStatus.WAR_CONTESTED) {
            return ProtectionConfig.ALLOW_EXPLOSIONS_IN_WAR_CHUNKS.get();
        }
        if (status == TerritoryStatus.PEACEFUL_CLAIMED || status == TerritoryStatus.POST_WAR_PROTECTED) {
            return isPeacefulExplosionAllowed(level, chunkPos);
        }
        return true;
    }

    public boolean isPartyExplosionProtectionEnabled(PartyId partyId) {
        return partyExplosionProtection.getOrDefault(partyId, true);
    }

    public boolean isPartyExplosionProtectionEnabled(MinecraftServer server, PartyId partyId) {
        Boolean locallyConfigured = partyExplosionProtection.get(partyId);
        if (locallyConfigured != null) {
            return locallyConfigured;
        }
        return readOpenPartiesExplosionProtection(server, partyId).orElse(true);
    }

    public boolean setPartyExplosionProtection(MinecraftServer server, PartyId partyId, boolean enabled) {
        partyExplosionProtection.put(partyId, enabled);
        syncOpenPartiesExplosionProtection(server, partyId, enabled);
        return true;
    }

    private Optional<Boolean> readOpenPartiesExplosionProtection(MinecraftServer server, PartyId partyId) {
        Optional<UUID> partyUuid = parsePartyUuid(partyId);
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
                partyConfig.getEffective(PlayerConfigOptions.CLAIM_EXCEPTION_BLOCKS_BY_EXPLOSIONS)
        );
        return Optional.of(!explosionsAllowed);
    }

    private boolean syncOpenPartiesExplosionProtection(MinecraftServer server, PartyId partyId, boolean enabled) {
        Optional<UUID> partyUuid = parsePartyUuid(partyId);
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
            success &= setExplosionException(partyConfig, explosionsAllowed);
        }

        for (UUID memberId : party.getMemberInfoStream().map(IPartyMemberAPI::getUUID).distinct().toList()) {
            IPlayerConfigAPI memberConfig = api.getPlayerConfigManager().getLoadedConfig(memberId);
            if (memberConfig != null) {
                applied = true;
                success &= setExplosionException(memberConfig, explosionsAllowed);
            }
        }

        return applied && success;
    }

    private boolean shouldBypassOpenPartiesExplosionProtection(ServerLevel level, ChunkPos chunkPos) {
        TerritoryStatus status = territoryService.getStatus(level, chunkPos);
        if (status == TerritoryStatus.WAR_CONTESTED) {
            boolean explosionsAllowed = ProtectionConfig.ALLOW_EXPLOSIONS_IN_WAR_CHUNKS.get();
            syncOpenPartiesClaimExplosionException(level, chunkPos, explosionsAllowed);
            return explosionsAllowed;
        }

        if (status == TerritoryStatus.PEACEFUL_CLAIMED || status == TerritoryStatus.POST_WAR_PROTECTED) {
            boolean explosionsAllowed = isPeacefulExplosionAllowed(level, chunkPos);
            syncOpenPartiesClaimExplosionException(level, chunkPos, explosionsAllowed);
            return explosionsAllowed;
        }

        return false;
    }

    private boolean isPeacefulExplosionAllowed(ServerLevel level, ChunkPos chunkPos) {
        Optional<PartyId> owner = territoryService.getClaimOwner(level, chunkPos);
        return owner
                .map(value -> !isPartyExplosionProtectionEnabled(level.getServer(), value))
                .orElse(false)
                || !ProtectionConfig.PROTECT_PEACEFUL_CLAIMS_FROM_EXPLOSIONS.get();
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
        return setExplosionException(api.getChunkProtection().getConfig(claim), explosionsAllowed);
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
