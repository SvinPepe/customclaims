package dev.customclaims.xaero.client;

import net.neoforged.neoforge.common.NeoForge;

public final class ClientEventRegistrar {
    private ClientEventRegistrar() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ClientOverlayService::onClientTick);
        NeoForge.EVENT_BUS.addListener(ClientOverlayService::onRenderGui);
    }
}
