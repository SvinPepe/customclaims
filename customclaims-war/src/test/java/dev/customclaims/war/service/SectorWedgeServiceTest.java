package dev.customclaims.war.service;

import dev.customclaims.war.service.SectorWedgeService.ChunkCoord;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SectorWedgeServiceTest {

    @Test
    @DisplayName("Single isolated claim chunk produces a wedge of size 1 at depth 1")
    void testSingleChunkClaim() {
        Set<Long> defenderChunks = Set.of(pack(0, 0));

        int depth = SectorWedgeService.calculateDepth(0, 0, 2, (x, z) -> defenderChunks.contains(pack(x, z)));
        assertEquals(1, depth, "Border chunk should have depth 1");

        Set<ChunkCoord> sector = SectorWedgeService.computeSectorCoords(
                0, 0, 4, 2,
                (x, z) -> defenderChunks.contains(pack(x, z)),
                (x, z) -> true
        );
        assertEquals(1, sector.size());
        assertTrue(sector.contains(new ChunkCoord(0, 0)));
    }

    @Test
    @DisplayName("Corner chunk with 3 valid neighbors builds a wedge up to max chunks")
    void testMultiChunkWedge() {
        // 2x2 grid of defender chunks
        Set<Long> defenderChunks = Set.of(
                pack(0, 0), pack(1, 0),
                pack(0, 1), pack(1, 1)
        );

        Set<ChunkCoord> sector = SectorWedgeService.computeSectorCoords(
                0, 0, 4, 2,
                (x, z) -> defenderChunks.contains(pack(x, z)),
                (x, z) -> true
        );

        assertEquals(4, sector.size(), "Should bundle all 4 connected chunks in the 2x2 sector");
        assertTrue(sector.contains(new ChunkCoord(0, 0)));
        assertTrue(sector.contains(new ChunkCoord(1, 0)));
        assertTrue(sector.contains(new ChunkCoord(0, 1)));
        assertTrue(sector.contains(new ChunkCoord(1, 1)));
    }

    @Test
    @DisplayName("Deep interior chunk (depth 3) is rejected from being targeted directly")
    void testDeepChunkRejection() {
        // 5x5 grid of defender chunks: (-2..2, -2..2)
        Set<Long> defenderChunks = new HashSet<>();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                defenderChunks.add(pack(x, z));
            }
        }

        int borderDepth = SectorWedgeService.calculateDepth(2, 0, 2, (x, z) -> defenderChunks.contains(pack(x, z)));
        int midDepth = SectorWedgeService.calculateDepth(1, 0, 2, (x, z) -> defenderChunks.contains(pack(x, z)));
        int centerDepth = SectorWedgeService.calculateDepth(0, 0, 2, (x, z) -> defenderChunks.contains(pack(x, z)));

        assertEquals(1, borderDepth, "Border chunk is depth 1");
        assertEquals(2, midDepth, "Mid chunk is depth 2");
        assertEquals(3, centerDepth, "Center chunk is depth 3 (deep interior)");

        Set<ChunkCoord> sector = SectorWedgeService.computeSectorCoords(
                0, 0, 4, 2,
                (x, z) -> defenderChunks.contains(pack(x, z)),
                (x, z) -> true
        );
        assertTrue(sector.isEmpty(), "Targeting center chunk at depth 3 must return empty sector");
    }

    @Test
    @DisplayName("Expansion skips chunks unavailable due to another active war")
    void testUnavailableChunksExcluded() {
        Set<Long> defenderChunks = Set.of(
                pack(0, 0),
                pack(1, 0),
                pack(0, 1)
        );

        // (1, 0) is locked by another war
        Set<ChunkCoord> sector = SectorWedgeService.computeSectorCoords(
                0, 0, 4, 2,
                (x, z) -> defenderChunks.contains(pack(x, z)),
                (x, z) -> !(x == 1 && z == 0) // (1, 0) unavailable
        );

        assertEquals(2, sector.size());
        assertTrue(sector.contains(new ChunkCoord(0, 0)));
        assertTrue(sector.contains(new ChunkCoord(0, 1)));
        assertFalse(sector.contains(new ChunkCoord(1, 0)), "Locked chunk must be excluded");
    }

    @Test
    @DisplayName("Expansion is bounded by maxChunks parameter")
    void testMaxChunksBound() {
        // 3x3 grid = 9 chunks
        Set<Long> defenderChunks = new HashSet<>();
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                defenderChunks.add(pack(x, z));
            }
        }

        Set<ChunkCoord> sector = SectorWedgeService.computeSectorCoords(
                0, 0, 3, 2,
                (x, z) -> defenderChunks.contains(pack(x, z)),
                (x, z) -> true
        );
        assertEquals(3, sector.size(), "Must not exceed maxChunks of 3");
    }

    private static long pack(int x, int z) {
        return (((long) x) << 32) | (z & 0xffffffffL);
    }
}
