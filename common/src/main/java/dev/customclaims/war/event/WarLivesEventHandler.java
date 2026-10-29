package dev.customclaims.war.event;

import dev.customclaims.war.CustomClaimsWarMod;
import net.minecraft.server.level.ServerPlayer;

public final class WarLivesEventHandler {
    private WarLivesEventHandler() {
    }

    public static void onPlayerDeath(ServerPlayer player) {
        if (player != null) {
            CustomClaimsWarMod.services().warManager().onPlayerDeath(player);
        }
    }
}
