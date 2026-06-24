package dev.customclaims.core.api.model;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public record ChunkPosKey(String levelId, int x, int z) {
    private static final String SEPARATOR = "|";

    public ChunkPosKey {
        Objects.requireNonNull(levelId, "levelId");
        if (levelId.isBlank()) {
            throw new IllegalArgumentException("levelId cannot be blank");
        }
    }

    public static ChunkPosKey from(ServerLevel level, ChunkPos chunkPos) {
        return new ChunkPosKey(level.dimension().location().toString(), chunkPos.x, chunkPos.z);
    }

    public static ChunkPosKey from(ServerLevel level, BlockPos blockPos) {
        return from(level, new ChunkPos(blockPos));
    }

    public ChunkPos toChunkPos() {
        return new ChunkPos(x, z);
    }

    public boolean isDirectNeighborOf(ChunkPosKey other) {
        return levelId.equals(other.levelId)
                && Math.abs(x - other.x) + Math.abs(z - other.z) == 1;
    }

    public boolean isDiagonalNeighborOf(ChunkPosKey other) {
        return levelId.equals(other.levelId)
                && Math.abs(x - other.x) == 1
                && Math.abs(z - other.z) == 1;
    }

    public String storageKey() {
        return levelId + SEPARATOR + x + SEPARATOR + z;
    }

    public static Optional<ChunkPosKey> parse(String value) {
        String[] parts = value.split("\\|", 3);
        if (parts.length != 3) {
            return Optional.empty();
        }

        try {
            return Optional.of(new ChunkPosKey(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }
}
