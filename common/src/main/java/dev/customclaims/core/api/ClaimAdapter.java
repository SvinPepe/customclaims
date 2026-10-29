package dev.customclaims.core.api;

import dev.customclaims.core.api.model.PartyId;
import dev.customclaims.core.api.model.ClaimSnapshot;
import dev.customclaims.core.api.model.ClaimSideId;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public interface ClaimAdapter {
    String name();

    /**
     * Returns the external claim system's canonical server-owned claim UUID.
     */
    UUID serverClaimOwnerId();

    Optional<PartyId> getClaimOwner(ServerLevel level, ChunkPos chunkPos);

    default Optional<ClaimSideId> getClaimSideOwner(ServerLevel level, ChunkPos chunkPos) {
        return getClaimOwner(level, chunkPos).map(ClaimSideId::party);
    }

    boolean transferClaim(ServerLevel level, ChunkPos chunkPos, PartyId newOwner);

    default boolean transferClaimToSide(ServerLevel level, ChunkPos chunkPos, ClaimSideId newOwner) {
        return newOwner.partyId()
                .map(partyId -> transferClaim(level, chunkPos, partyId))
                .orElse(false);
    }

    Optional<ClaimSnapshot> getClaimSnapshot(ServerLevel level, ChunkPos chunkPos);

    /**
     * Administratively assigns a claim to {@code ownerId} from the server context.
     *
     * <p>This must bypass player claim requests, including player claim limits and
     * economy hooks attached to the normal player claiming path. The owner UUID is
     * still the resulting claim owner; it is not the actor performing the change.</p>
     */
    boolean claimFromServer(
            ServerLevel level,
            ChunkPos chunkPos,
            UUID ownerId,
            int subConfigIndex,
            boolean forceload
    );

    default boolean isClaimed(ServerLevel level, ChunkPos chunkPos) {
        return getClaimSideOwner(level, chunkPos).isPresent();
    }
}
