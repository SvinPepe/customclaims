package dev.customclaims.xaero.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class XaeroNetwork {
    private XaeroNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(
                ClientboundWarMarkersPayload.TYPE,
                ClientboundWarMarkersPayload.STREAM_CODEC,
                ClientboundWarMarkersPayload::handle
        );
    }
}
