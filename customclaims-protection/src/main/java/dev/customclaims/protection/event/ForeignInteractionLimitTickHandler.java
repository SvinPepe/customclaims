package dev.customclaims.protection.event;

import dev.customclaims.protection.CustomClaimsProtectionMod;
import java.time.Instant;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class ForeignInteractionLimitTickHandler {
    private static long ticks;

    private ForeignInteractionLimitTickHandler() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        CustomClaimsProtectionMod.services().openPartiesProtectionBypassService().clearFullPasses();

        ticks++;
        if (ticks % 20L != 0L) {
            return;
        }
        CustomClaimsProtectionMod.services().foreignInteractionLimitService().tickResetIfDue(Instant.now());
    }
}
