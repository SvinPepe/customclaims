package dev.customclaims.protection.service;

import dev.customclaims.core.api.model.ClaimSideId;

public record ClaimRulesState(
        boolean hasSide,
        ClaimSideId sideId,
        String sideLabel,
        boolean explosionProtectionEnabled,
        boolean createMachinesEnabled,
        boolean assemblyEnabled,
        long explosionCooldownSeconds,
        long createCooldownSeconds,
        long assemblyCooldownSeconds,
        boolean canToggleExplosions,
        boolean canToggleCreate,
        boolean canToggleAssembly
) {
    public static ClaimRulesState noSide(
            boolean canToggleExplosions,
            boolean canToggleCreate,
            boolean canToggleAssembly
    ) {
        return new ClaimRulesState(
                false,
                null,
                "No claim owner",
                false,
                false,
                false,
                0L,
                0L,
                0L,
                canToggleExplosions,
                canToggleCreate,
                canToggleAssembly
        );
    }
}
