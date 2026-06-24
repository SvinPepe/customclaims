package dev.customclaims.war.event;

import dev.customclaims.war.CustomClaimsWarMod;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class WarTickHandler {
    private WarTickHandler() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        CustomClaimsWarMod.services().warManager().tick(event.getServer());
    }
}
