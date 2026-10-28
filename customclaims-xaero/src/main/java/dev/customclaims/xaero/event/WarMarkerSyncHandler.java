package dev.customclaims.xaero.event;

import dev.customclaims.core.platform.PlatformHelperHolder;
import dev.customclaims.war.CustomClaimsWarMod;
import dev.customclaims.war.model.WarMarkerDto;
import dev.customclaims.xaero.config.XaeroCompatConfig;
import dev.customclaims.xaero.network.ClientboundWarMarkersPayload;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class WarMarkerSyncHandler {
    private static long ticks;

    private WarMarkerSyncHandler() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        ticks++;
        if (ticks % 20 != 0) {
            return;
        }

        int radius = XaeroCompatConfig.VISIBLE_RADIUS_CHUNKS.get();
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!PlatformHelperHolder.getNetwork().canSendToPlayer(player, ClientboundWarMarkersPayload.TYPE.id())) {
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
