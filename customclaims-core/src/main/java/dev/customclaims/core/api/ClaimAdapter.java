package dev.customclaims.core.api;

import dev.customclaims.core.api.model.PartyId;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public interface ClaimAdapter {
    String name();

    Optional<PartyId> getClaimOwner(ServerLevel level, ChunkPos chunkPos);

    boolean transferClaim(ServerLevel level, ChunkPos chunkPos, PartyId newOwner);

    default boolean isClaimed(ServerLevel level, ChunkPos chunkPos) {
        return getClaimOwner(level, chunkPos).isPresent();
    }
}
