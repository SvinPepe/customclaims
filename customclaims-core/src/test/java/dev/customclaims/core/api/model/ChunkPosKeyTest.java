package dev.customclaims.core.api.model;

import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChunkPosKeyTest {

    @Test
    @DisplayName("ChunkPosKey from null level falls back to minecraft:overworld")
    void testFromNullLevelFallback() {
        ChunkPosKey key = ChunkPosKey.from(null, new ChunkPos(3, 7));
        assertEquals("minecraft:overworld", key.levelId());
        assertEquals(3, key.x());
        assertEquals(7, key.z());
    }

    @Test
    @DisplayName("ChunkPosKey conversion to ChunkPos retains coordinates")
    void testToChunkPos() {
        ChunkPosKey key = new ChunkPosKey("minecraft:the_nether", -4, 12);
        ChunkPos pos = key.toChunkPos();
        assertEquals(-4, pos.x);
        assertEquals(12, pos.z);
    }

    @Test
    @DisplayName("isDirectNeighborOf correctly identifies orthogonal neighbors")
    void testDirectNeighbor() {
        ChunkPosKey center = new ChunkPosKey("minecraft:overworld", 10, 10);

        assertTrue(center.isDirectNeighborOf(new ChunkPosKey("minecraft:overworld", 11, 10)));
        assertTrue(center.isDirectNeighborOf(new ChunkPosKey("minecraft:overworld", 9, 10)));
        assertTrue(center.isDirectNeighborOf(new ChunkPosKey("minecraft:overworld", 10, 11)));
        assertTrue(center.isDirectNeighborOf(new ChunkPosKey("minecraft:overworld", 10, 9)));

        // Diagonal is NOT a direct neighbor
        assertFalse(center.isDirectNeighborOf(new ChunkPosKey("minecraft:overworld", 11, 11)));

        // Different dimension is not a neighbor
        assertFalse(center.isDirectNeighborOf(new ChunkPosKey("minecraft:the_nether", 11, 10)));
    }

    @Test
    @DisplayName("isDiagonalNeighborOf correctly identifies corner neighbors")
    void testDiagonalNeighbor() {
        ChunkPosKey center = new ChunkPosKey("minecraft:overworld", 10, 10);

        assertTrue(center.isDiagonalNeighborOf(new ChunkPosKey("minecraft:overworld", 11, 11)));
        assertTrue(center.isDiagonalNeighborOf(new ChunkPosKey("minecraft:overworld", 9, 9)));
        assertTrue(center.isDiagonalNeighborOf(new ChunkPosKey("minecraft:overworld", 11, 9)));
        assertTrue(center.isDiagonalNeighborOf(new ChunkPosKey("minecraft:overworld", 9, 11)));

        // Cardinal is NOT a diagonal neighbor
        assertFalse(center.isDiagonalNeighborOf(new ChunkPosKey("minecraft:overworld", 10, 11)));

        // Same position is not a diagonal neighbor
        assertFalse(center.isDiagonalNeighborOf(center));

        // Different dimension is not a neighbor
        assertFalse(center.isDiagonalNeighborOf(new ChunkPosKey("minecraft:the_nether", 11, 11)));
    }

    @Test
    @DisplayName("Serialization string round-trips via storageKey and parse")
    void testStringSerialization() {
        ChunkPosKey key = new ChunkPosKey("minecraft:the_end", 42, -99);
        String str = key.storageKey();
        var parsed = ChunkPosKey.parse(str);
        assertTrue(parsed.isPresent());
        assertEquals(key, parsed.get());
    }
}
