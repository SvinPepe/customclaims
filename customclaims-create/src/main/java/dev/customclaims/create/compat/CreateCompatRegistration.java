package dev.customclaims.create.compat;

import com.simibubi.create.api.contraption.BlockMovementChecks;
import dev.customclaims.create.CustomClaimsCreateMod;

public final class CreateCompatRegistration {
    private static boolean registered;

    private CreateCompatRegistration() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }

        BlockMovementChecks.registerMovementAllowedCheck((state, level, pos) -> {
            if (CreateContraptionProtectionHooks.shouldBlockMovement(level, pos)) {
                return BlockMovementChecks.CheckResult.FAIL;
            }
            return BlockMovementChecks.CheckResult.PASS;
        });
        registered = true;
        CustomClaimsCreateMod.LOGGER.info("Registered CustomClaims Create movement checks");
    }
}
