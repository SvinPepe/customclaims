package dev.customclaims.core.rollback;

import java.util.List;
import java.util.UUID;

public final class NoopRollbackService implements RollbackService {
    @Override
    public void recordBlockChange(BlockChangeRecord record) {
        // TODO: Persist block changes when rollback implementation is introduced.
    }

    @Override
    public List<RollbackEntry> entriesForWar(UUID warId) {
        return List.of();
    }
}
