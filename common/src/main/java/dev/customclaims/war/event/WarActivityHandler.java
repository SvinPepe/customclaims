package dev.customclaims.war.event;

import dev.customclaims.war.CustomClaimsWarMod;
import net.minecraft.server.level.ServerPlayer;

public final class WarActivityHandler {
    private WarActivityHandler() {
    }

    public static void onPlayerActivity(ServerPlayer player) {
        if (player != null) {
            CustomClaimsWarMod.services().afkTracker().markActive(player);
        }
    }
}
