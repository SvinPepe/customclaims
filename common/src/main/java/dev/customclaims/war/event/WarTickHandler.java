package dev.customclaims.war.event;

import dev.customclaims.war.CustomClaimsWarMod;
import net.minecraft.server.MinecraftServer;

public final class WarTickHandler {
    private WarTickHandler() {
    }

    public static void onServerTick(MinecraftServer server) {
        CustomClaimsWarMod.services().warManager().tick(server);
    }
}
