package dev.customclaims.core.service;

import dev.customclaims.core.api.ClaimAdapter;
import dev.customclaims.core.api.model.ClaimSideId;
import dev.customclaims.core.api.model.PartyId;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public final class ClaimService {
    private final ClaimAdapter claimAdapter;

    public ClaimService(ClaimAdapter claimAdapter) {
        this.claimAdapter = claimAdapter;
    }

    public Optional<PartyId> getOwner(ServerLevel level, ChunkPos chunkPos) {
        return claimAdapter.getClaimOwner(level, chunkPos);
    }

    public Optional<ClaimSideId> getSideOwner(ServerLevel level, ChunkPos chunkPos) {
        return claimAdapter.getClaimSideOwner(level, chunkPos);
    }

    public boolean isClaimed(ServerLevel level, ChunkPos chunkPos) {
        return claimAdapter.isClaimed(level, chunkPos);
    }

    public boolean transfer(ServerLevel level, ChunkPos chunkPos, PartyId newOwner) {
        return claimAdapter.transferClaim(level, chunkPos, newOwner);
    }

    public boolean transferToSide(ServerLevel level, ChunkPos chunkPos, ClaimSideId newOwner) {
        return claimAdapter.transferClaimToSide(level, chunkPos, newOwner);
    }
}
