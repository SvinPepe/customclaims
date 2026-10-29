package dev.customclaims.war.service;

import dev.customclaims.core.api.model.ClaimSideId;
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

    public boolean isBorderChunk(ServerLevel level, ChunkPos target, ClaimSideId attacker, ClaimSideId defender, boolean includeDiagonal) {
        boolean defenderOwnsTarget = territoryService.getClaimOwnerSide(level, target).filter(defender::equals).isPresent();
        if (!defenderOwnsTarget) {
            return false;
        }

        if (hasAttackableNeighbor(level, target, attacker, DIRECT_NEIGHBORS)) {
            return true;
        }

        return includeDiagonal && hasAttackableNeighbor(level, target, attacker, DIAGONAL_NEIGHBORS);
    }

    private boolean hasAttackableNeighbor(ServerLevel level, ChunkPos target, ClaimSideId attacker, int[][] offsets) {
        //? if <26.0 {
        int tx = target.x;
        int tz = target.z;
        //?} else {
        /*int tx = target.x();
        int tz = target.z();
        *///?}
        for (int[] offset : offsets) {
            ChunkPos neighbor = new ChunkPos(tx + offset[0], tz + offset[1]);
            java.util.Optional<ClaimSideId> owner = territoryService.getClaimOwnerSide(level, neighbor);
            if (owner.isEmpty() || owner.filter(attacker::equals).isPresent()) {
                return true;
            }
        }
        return false;
    }
}
