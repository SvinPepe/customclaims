package dev.customclaims.protection.service;

import dev.customclaims.core.api.model.PartyId;

public record ClaimRulesState(
        boolean inParty,
        PartyId partyId,
        String partyLabel,
        boolean explosionProtectionEnabled,
        boolean createMachinesEnabled,
        long explosionCooldownSeconds,
        long createCooldownSeconds,
        boolean canToggleExplosions,
        boolean canToggleCreate
) {
    public static ClaimRulesState noParty(boolean canToggleExplosions, boolean canToggleCreate) {
        return new ClaimRulesState(
                false,
                null,
                "No party",
                false,
                false,
                0L,
                0L,
                canToggleExplosions,
                canToggleCreate
        );
    }
}
