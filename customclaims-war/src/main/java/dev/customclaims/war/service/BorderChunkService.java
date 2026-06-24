package dev.customclaims.war.service;

import dev.customclaims.core.api.model.PartyId;
import dev.customclaims.core.service.TerritoryService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public final class BorderChunkService {
    private static final int[][] DIRECT_NEIGHBORS = {
            {1, 0},
            {-1, 0},
            {0, 1},
            {0, -1}
    };
    private static final int[][] DIAGONAL_NEIGHBORS = {
            {1, 1},
            {1, -1},
            {-1, 1},
            {-1, -1}
    };

    private final TerritoryService territoryService;

    public BorderChunkService(TerritoryService territoryService) {
        this.territoryService = territoryService;
    }

    public boolean isBorderChunk(ServerLevel level, ChunkPos target, PartyId attacker, PartyId defender, boolean includeDiagonal) {
        boolean defenderOwnsTarget = territoryService.getClaimOwner(level, target).filter(defender::equals).isPresent();
        if (!defenderOwnsTarget) {
            return false;
        }

        if (hasAttackableNeighbor(level, target, attacker, DIRECT_NEIGHBORS)) {
            return true;
        }

        return includeDiagonal && hasAttackableNeighbor(level, target, attacker, DIAGONAL_NEIGHBORS);
    }

    private boolean hasAttackableNeighbor(ServerLevel level, ChunkPos target, PartyId attacker, int[][] offsets) {
        for (int[] offset : offsets) {
            ChunkPos neighbor = new ChunkPos(target.x + offset[0], target.z + offset[1]);
            java.util.Optional<PartyId> owner = territoryService.getClaimOwner(level, neighbor);
            if (owner.isEmpty() || owner.filter(attacker::equals).isPresent()) {
                return true;
            }
        }
        return false;
    }
}
