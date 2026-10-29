package dev.customclaims.xaero.client;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class ClientEventRegistrar {
    private ClientEventRegistrar() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> ClientOverlayService.onClientTick());
        NeoForge.EVENT_BUS.addListener((RenderGuiEvent.Post event) -> ClientOverlayService.onRenderGui(event.getGuiGraphics()));
    }
}
