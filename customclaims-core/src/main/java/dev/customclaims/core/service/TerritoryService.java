package dev.customclaims.core.service;

import dev.customclaims.core.api.ClaimAdapter;
import dev.customclaims.core.api.PartyAdapter;
import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.api.model.ClaimSnapshot;
import dev.customclaims.core.api.model.ClaimSideId;
import dev.customclaims.core.api.model.PartyId;
import dev.customclaims.core.api.model.TerritoryRelation;
import dev.customclaims.core.api.model.TerritoryStatus;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public final class TerritoryService {
    private final ClaimAdapter claimAdapter;
    private final PartyAdapter partyAdapter;
    private final TerritoryStateService territoryStateService;

    public TerritoryService(
            ClaimAdapter claimAdapter,
            PartyAdapter partyAdapter,
            TerritoryStateService territoryStateService
    ) {
        this.claimAdapter = claimAdapter;
        this.partyAdapter = partyAdapter;
        this.territoryStateService = territoryStateService;
    }

    public Optional<PartyId> getClaimOwner(ServerLevel level, ChunkPos chunkPos) {
        return getClaimOwnerSide(level, chunkPos).flatMap(ClaimSideId::partyId);
    }

    public Optional<ClaimSideId> getClaimOwnerSide(ServerLevel level, ChunkPos chunkPos) {
        ChunkPosKey key = ChunkPosKey.from(level, chunkPos);
        return territoryStateService.ownerOverride(key)
                .or(() -> claimAdapter.getClaimSideOwner(level, chunkPos));
    }

    public TerritoryStatus getStatus(ServerLevel level, ChunkPos chunkPos) {
        ChunkPosKey key = ChunkPosKey.from(level, chunkPos);
        Optional<TerritoryStatus> override = territoryStateService.statusOverride(key);
        if (override.isPresent()) {
            return override.get();
        }

        return getClaimOwnerSide(level, chunkPos).isPresent()
                ? TerritoryStatus.PEACEFUL_CLAIMED
                : TerritoryStatus.UNCLAIMED;
    }

    public TerritoryStatus getInteractionStatus(ServerPlayer player, ServerLevel level, ChunkPos chunkPos) {
            TerritoryStatus status = getStatus(level, chunkPos);
        if (status == TerritoryStatus.WAR_CONTESTED) {
            ChunkPosKey key = ChunkPosKey.from(level, chunkPos);
            ClaimSideId currentSide = partyAdapter.getPlayerParty(player)
                    .map(ClaimSideId::party)
                    .orElseGet(() -> ClaimSideId.player(player.getUUID()));
            ClaimSideId personalSide = ClaimSideId.player(player.getUUID());
            return territoryStateService.isContestedParticipant(key, currentSide)
                    || territoryStateService.isContestedParticipant(key, personalSide)
                    ? TerritoryStatus.WAR_CONTESTED
                    : TerritoryStatus.FOREIGN_LIMITED_INTERACTION;
        }
        if (status != TerritoryStatus.PEACEFUL_CLAIMED) {
            return status;
        }

        return getRelation(player, level, chunkPos) == TerritoryRelation.FOREIGN
                ? TerritoryStatus.FOREIGN_LIMITED_INTERACTION
                : TerritoryStatus.PEACEFUL_CLAIMED;
    }

    public TerritoryRelation getRelation(ServerPlayer player, ServerLevel level, ChunkPos chunkPos) {
        Optional<ClaimSideId> owner = getClaimOwnerSide(level, chunkPos);
        if (owner.isEmpty()) {
            return TerritoryRelation.UNCLAIMED;
        }
        return isPlayerInSide(player, owner.get())
                ? TerritoryRelation.OWN
                : TerritoryRelation.FOREIGN;
    }

    public boolean isWarContested(ServerLevel level, ChunkPos chunkPos) {
        return getStatus(level, chunkPos) == TerritoryStatus.WAR_CONTESTED;
    }

    public boolean transferClaim(ServerLevel level, ChunkPos chunkPos, PartyId newOwner) {
        return claimAdapter.transferClaim(level, chunkPos, newOwner);
    }

    public boolean transferClaimToSide(ServerLevel level, ChunkPos chunkPos, ClaimSideId newOwner) {
        return claimAdapter.transferClaimToSide(level, chunkPos, newOwner);
    }

    public Optional<ClaimSnapshot> getClaimSnapshot(ServerLevel level, ChunkPos chunkPos) {
        return claimAdapter.getClaimSnapshot(level, chunkPos);
    }

    public boolean claimForPlayer(ServerLevel level, ChunkPos chunkPos, UUID ownerId, int subConfigIndex, boolean forceload) {
        return claimAdapter.claimForPlayer(level, chunkPos, ownerId, subConfigIndex, forceload);
    }

    private boolean isPlayerInSide(ServerPlayer player, ClaimSideId sideId) {
        if (sideId.isPlayer()) {
            return sideId.playerUuid().filter(player.getUUID()::equals).isPresent();
        }
        return sideId.partyId().map(partyId -> partyAdapter.isPlayerInParty(player, partyId)).orElse(false);
    }
}
