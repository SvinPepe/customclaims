package dev.customclaims.core.api.model;

import java.util.UUID;

public record ClaimSnapshot(
        UUID ownerId,
        boolean partyOwned,
        int subConfigIndex,
        boolean forceload
) {
}
