package dev.customclaims.core.api;

import dev.customclaims.core.api.model.PartyId;
import dev.customclaims.core.api.model.ClaimSnapshot;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public interface ClaimAdapter {
    String name();

    Optional<PartyId> getClaimOwner(ServerLevel level, ChunkPos chunkPos);

    boolean transferClaim(ServerLevel level, ChunkPos chunkPos, PartyId newOwner);

    Optional<ClaimSnapshot> getClaimSnapshot(ServerLevel level, ChunkPos chunkPos);

    boolean claimForPlayer(ServerLevel level, ChunkPos chunkPos, UUID ownerId, int subConfigIndex, boolean forceload);

    default boolean isClaimed(ServerLevel level, ChunkPos chunkPos) {
        return getClaimOwner(level, chunkPos).isPresent();
    }
}
