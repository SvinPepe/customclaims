package dev.customclaims.xaero.event;

import dev.customclaims.core.platform.PlatformHelperHolder;
import dev.customclaims.war.CustomClaimsWarMod;
import dev.customclaims.war.model.WarMarkerDto;
import dev.customclaims.xaero.config.XaeroCompatConfig;
import dev.customclaims.xaero.network.ClientboundWarMarkersPayload;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class WarMarkerSyncHandler {
    private static long ticks = 0;

    private WarMarkerSyncHandler() {
    }

    public static void onServerTick(MinecraftServer server) {
        ticks++;
        if (ticks % 20 != 0) {
            return;
        }

        int radius = XaeroCompatConfig.VISIBLE_RADIUS_CHUNKS.get();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            //? if >=1.20.2 {
            if (!PlatformHelperHolder.getNetwork().canSendToPlayer(player, ClientboundWarMarkersPayload.TYPE.id().toString())) {
            //?} else {
            /*if (!PlatformHelperHolder.getNetwork().canSendToPlayer(player, ClientboundWarMarkersPayload.ID.toString())) {
            *///?}
                continue;
            }
            List<WarMarkerDto> markers = CustomClaimsWarMod.services()
                    .warManager()
                    .visibleMarkersFor(
                            player,
                            radius,
                            XaeroCompatConfig.VISIBLE_TO_ALL_PLAYERS.get()
                    );
            PlatformHelperHolder.getNetwork().sendToPlayer(player, new ClientboundWarMarkersPayload(markers));
        }
    }
}
