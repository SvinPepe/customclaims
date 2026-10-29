package dev.customclaims.fabric.client;

import dev.customclaims.xaero.client.ClientOverlayService;
import dev.customclaims.xaero.client.WarClientPacketHandler;
import dev.customclaims.xaero.network.ClientboundWarMarkersPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public final class CustomClaimsFabricClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientOverlayService.onClientTick());
        HudRenderCallback.EVENT.register((graphics, delta) -> ClientOverlayService.onRenderGui(graphics));

        //? if >=1.20.5 {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundWarMarkersPayload.TYPE, (payload, context) -> {
            WarClientPacketHandler.handle(payload);
        });
        //?} else {
        /*ClientPlayNetworking.registerGlobalReceiver(ClientboundWarMarkersPayload.ID, (client, handler, buf, responseSender) -> {
            ClientboundWarMarkersPayload payload = ClientboundWarMarkersPayload.read(buf);
            client.execute(() -> WarClientPacketHandler.handle(payload));
        });
        *///?}
    }
}
