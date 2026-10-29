package dev.customclaims.xaero.network;

import net.minecraft.server.level.ServerPlayer;
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
                (payload, context) -> context.enqueueWork(() -> ClientboundWarMarkersPayload.handle(payload))
        );
        registrar.playToServer(
                ServerboundStartWarAtPayload.TYPE,
                ServerboundStartWarAtPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        ServerboundStartWarAtPayload.handle(payload, player);
                    }
                })
        );
    }
}
