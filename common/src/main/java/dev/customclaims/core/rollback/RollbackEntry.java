package dev.customclaims.core.rollback;

import java.time.Instant;
import java.util.UUID;

public record RollbackEntry(
        UUID id,
        UUID warId,
        BlockChangeRecord change,
        Instant recordedAt
) {
}
