package dev.customclaims.xaero.client;

import dev.customclaims.xaero.network.ClientboundWarMarkersPayload;
import dev.customclaims.xaero.service.XaeroWaypointService;

public final class WarClientPacketHandler {
    private WarClientPacketHandler() {
    }

    public static void handle(ClientboundWarMarkersPayload payload) {
        XaeroWaypointService.replaceWarMarkers(payload.markers());
        ClientOverlayService.replaceMarkers(payload.markers());
    }
}
