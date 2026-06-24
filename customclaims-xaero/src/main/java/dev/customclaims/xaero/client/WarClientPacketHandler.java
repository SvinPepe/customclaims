package dev.customclaims.xaero.client;

import dev.customclaims.xaero.network.ClientboundWarMarkersPayload;

public final class WarClientPacketHandler {
    private WarClientPacketHandler() {
    }

    public static void handle(ClientboundWarMarkersPayload payload) {
        ClientOverlayService.replaceMarkers(payload.markers());
    }
}
