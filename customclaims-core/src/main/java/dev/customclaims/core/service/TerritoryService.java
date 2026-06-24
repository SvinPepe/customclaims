package dev.customclaims.core.service;

import dev.customclaims.core.api.ClaimAdapter;
import dev.customclaims.core.api.PartyAdapter;
import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.api.model.PartyId;
import dev.customclaims.core.api.model.TerritoryRelation;
import dev.customclaims.core.api.model.TerritoryStatus;
import java.util.Optional;
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
        ChunkPosKey key = ChunkPosKey.from(level, chunkPos);
        return territoryStateService.ownerOverride(key)
                .or(() -> claimAdapter.getClaimOwner(level, chunkPos));
    }

    public TerritoryStatus getStatus(ServerLevel level, ChunkPos chunkPos) {
        ChunkPosKey key = ChunkPosKey.from(level, chunkPos);
        Optional<TerritoryStatus> override = territoryStateService.statusOverride(key);
        if (override.isPresent()) {
            return override.get();
        }

        return getClaimOwner(level, chunkPos).isPresent()
                ? TerritoryStatus.PEACEFUL_CLAIMED
                : TerritoryStatus.UNCLAIMED;
    }

    public TerritoryStatus getInteractionStatus(ServerPlayer player, ServerLevel level, ChunkPos chunkPos) {
        TerritoryStatus status = getStatus(level, chunkPos);
        if (status != TerritoryStatus.PEACEFUL_CLAIMED) {
            return status;
        }

        return getRelation(player, level, chunkPos) == TerritoryRelation.FOREIGN
                ? TerritoryStatus.FOREIGN_LIMITED_INTERACTION
                : TerritoryStatus.PEACEFUL_CLAIMED;
    }

    public TerritoryRelation getRelation(ServerPlayer player, ServerLevel level, ChunkPos chunkPos) {
        Optional<PartyId> owner = getClaimOwner(level, chunkPos);
        if (owner.isEmpty()) {
            return TerritoryRelation.UNCLAIMED;
        }
        return partyAdapter.isPlayerInParty(player, owner.get())
                ? TerritoryRelation.OWN
                : TerritoryRelation.FOREIGN;
    }

    public boolean isWarContested(ServerLevel level, ChunkPos chunkPos) {
        return getStatus(level, chunkPos) == TerritoryStatus.WAR_CONTESTED;
    }

    public boolean transferClaim(ServerLevel level, ChunkPos chunkPos, PartyId newOwner) {
        boolean transferred = claimAdapter.transferClaim(level, chunkPos, newOwner);
        if (!transferred) {
            territoryStateService.setOwnerOverride(ChunkPosKey.from(level, chunkPos), newOwner);
        }
        return true;
    }
}
