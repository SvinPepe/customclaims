package dev.customclaims.war.service;

import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.api.model.ClaimSideId;
import dev.customclaims.core.service.TerritoryService;
import dev.customclaims.war.config.WarConfig;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public final class SectorWedgeService {
    private static final int[][] CARDINAL_OFFSETS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private final TerritoryService territoryService;

    public SectorWedgeService(TerritoryService territoryService) {
        this.territoryService = territoryService;
    }

    public record ChunkCoord(int x, int z) {}

    public record SectorResult(boolean success, Set<ChunkPosKey> sectorChunks, String errorMessage) {
        public static SectorResult ok(Set<ChunkPosKey> chunks) {
            return new SectorResult(true, chunks, "");
        }

        public static SectorResult fail(String error) {
            return new SectorResult(false, Set.of(), error);
        }
    }

    public SectorResult buildConquestSector(ServerLevel level, ChunkPos targetPos, ClaimSideId defenderSide) {
        return buildConquestSector(level, targetPos, defenderSide, key -> true);
    }

    public SectorResult buildConquestSector(
            ServerLevel level,
            ChunkPos targetPos,
            ClaimSideId defenderSide,
            Predicate<ChunkPosKey> isChunkAvailable
    ) {
        int maxDepth = WarConfig.getSafe(WarConfig.MAX_CONQUEST_DEPTH_CHUNKS);
        int targetDepth = calculateDepth(level, targetPos, defenderSide);
        if (targetDepth > maxDepth) {
            return SectorResult.fail("Cannot target deep claim chunk! Max conquest depth is "
                    + maxDepth + " chunks from border (target is depth " + targetDepth + ").");
        }

        int maxChunks = WarConfig.getSafe(WarConfig.MAX_SECTOR_CHUNKS);
        String levelId = level != null && level.dimension() != null && level.dimension().location() != null
                ? level.dimension().location().toString()
                : "minecraft:overworld";

        Set<ChunkCoord> coords = computeSectorCoords(
                targetPos.x,
                targetPos.z,
                maxChunks,
                maxDepth,
                (x, z) -> isDefenderChunk(level, new ChunkPos(x, z), defenderSide),
                (x, z) -> isChunkAvailable.test(new ChunkPosKey(levelId, x, z))
        );

        Set<ChunkPosKey> result = new LinkedHashSet<>();
        for (ChunkCoord coord : coords) {
            result.add(new ChunkPosKey(levelId, coord.x(), coord.z()));
        }
        return SectorResult.ok(result);
    }

    public int calculateDepth(ServerLevel level, ChunkPos pos, ClaimSideId defenderSide) {
        int maxDepth = WarConfig.getSafe(WarConfig.MAX_CONQUEST_DEPTH_CHUNKS);
        return calculateDepth(pos.x, pos.z, maxDepth, (x, z) -> isDefenderChunk(level, new ChunkPos(x, z), defenderSide));
    }

    public static int calculateDepth(
            int startX,
            int startZ,
            int maxDepth,
            BiPredicate<Integer, Integer> isDefender
    ) {
        if (!isDefender.test(startX, startZ)) {
            return 0;
        }

        Set<Long> visited = new HashSet<>();
        Queue<int[]> queue = new ArrayDeque<>();

        visited.add(pack(startX, startZ));
        queue.add(new int[]{startX, startZ, 1});

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int currX = current[0];
            int currZ = current[1];
            int depth = current[2];

            for (int[] offset : CARDINAL_OFFSETS) {
                int nx = currX + offset[0];
                int nz = currZ + offset[1];
                if (!isDefender.test(nx, nz)) {
                    return depth;
                }
                if (depth < maxDepth && visited.add(pack(nx, nz))) {
                    queue.add(new int[]{nx, nz, depth + 1});
                }
            }
        }

        return maxDepth + 1;
    }

    public static Set<ChunkCoord> computeSectorCoords(
            int targetX,
            int targetZ,
            int maxChunks,
            int maxDepth,
            BiPredicate<Integer, Integer> isDefender,
            BiPredicate<Integer, Integer> isAvailable
    ) {
        int targetDepth = calculateDepth(targetX, targetZ, maxDepth, isDefender);
        if (targetDepth > maxDepth) {
            return Set.of();
        }

        Set<ChunkCoord> sector = new LinkedHashSet<>();
        Set<Long> visited = new HashSet<>();
        Queue<ChunkCoord> queue = new ArrayDeque<>();

        ChunkCoord targetCoord = new ChunkCoord(targetX, targetZ);
        sector.add(targetCoord);
        visited.add(pack(targetX, targetZ));
        queue.add(targetCoord);

        while (!queue.isEmpty() && sector.size() < maxChunks) {
            ChunkCoord current = queue.poll();
            for (int[] offset : CARDINAL_OFFSETS) {
                if (sector.size() >= maxChunks) {
                    break;
                }
                int nx = current.x() + offset[0];
                int nz = current.z() + offset[1];
                long packed = pack(nx, nz);
                if (visited.contains(packed)) {
                    continue;
                }

                if (isDefender.test(nx, nz) && isAvailable.test(nx, nz)) {
                    int neighborDepth = calculateDepth(nx, nz, maxDepth, isDefender);
                    if (neighborDepth <= maxDepth) {
                        ChunkCoord neighbor = new ChunkCoord(nx, nz);
                        sector.add(neighbor);
                        visited.add(packed);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return sector;
    }

    private static long pack(int x, int z) {
        return (((long) x) << 32) | (z & 0xffffffffL);
    }

    private boolean isDefenderChunk(ServerLevel level, ChunkPos pos, ClaimSideId defenderSide) {
        Optional<ClaimSideId> owner = territoryService.getClaimOwnerSide(level, pos);
        return owner.isPresent() && owner.get().equals(defenderSide);
    }
}
