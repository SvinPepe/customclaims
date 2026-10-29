package dev.customclaims.core.rollback;

import dev.customclaims.core.api.model.ChunkPosKey;
import java.time.Instant;

public record BlockChangeRecord(
        ChunkPosKey chunk,
        int blockX,
        int blockY,
        int blockZ,
        String beforeBlockState,
        String afterBlockState,
        String actor,
        Instant happenedAt
) {
}
