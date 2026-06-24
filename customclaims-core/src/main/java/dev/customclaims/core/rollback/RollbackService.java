package dev.customclaims.core.rollback;

import java.util.List;
import java.util.UUID;

public interface RollbackService {
    void recordBlockChange(BlockChangeRecord record);

    List<RollbackEntry> entriesForWar(UUID warId);
}
