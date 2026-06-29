package dev.customclaims.protection.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public final class ClaimRulesClientEventRegistrar {
    private ClaimRulesClientEventRegistrar() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ClaimRulesKeyHandler::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(ClaimRulesKeyHandler::onClientTick);
    }
}
