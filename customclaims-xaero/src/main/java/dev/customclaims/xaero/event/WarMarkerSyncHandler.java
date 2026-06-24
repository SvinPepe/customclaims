package dev.customclaims.xaero.event;

import dev.customclaims.war.CustomClaimsWarMod;
import dev.customclaims.war.model.WarMarkerDto;
import dev.customclaims.xaero.config.XaeroCompatConfig;
import dev.customclaims.xaero.network.ClientboundWarMarkersPayload;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

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
            if (!NetworkRegistry.hasChannel(player.connection, ClientboundWarMarkersPayload.TYPE.id())) {
                continue;
            }
            List<WarMarkerDto> markers = CustomClaimsWarMod.services()
                    .warManager()
                    .visibleMarkersFor(player, radius);
            PacketDistributor.sendToPlayer(player, new ClientboundWarMarkersPayload(markers));
        }
    }
}
