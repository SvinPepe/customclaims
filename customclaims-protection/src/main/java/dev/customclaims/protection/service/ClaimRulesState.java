package dev.customclaims.protection.service;

import dev.customclaims.core.api.model.ClaimSideId;

public record ClaimRulesState(
        boolean hasSide,
        ClaimSideId sideId,
        String sideLabel,
        boolean explosionProtectionEnabled,
        boolean createMachinesEnabled,
        long explosionCooldownSeconds,
        long createCooldownSeconds,
        boolean canToggleExplosions,
        boolean canToggleCreate
) {
    public static ClaimRulesState noSide(boolean canToggleExplosions, boolean canToggleCreate) {
        return new ClaimRulesState(
                false,
                null,
                "No claim owner",
                false,
                false,
                0L,
                0L,
                canToggleExplosions,
                canToggleCreate
        );
    }
}
