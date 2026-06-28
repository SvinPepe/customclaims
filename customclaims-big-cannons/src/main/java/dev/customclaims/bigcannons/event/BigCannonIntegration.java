package dev.customclaims.bigcannons.event;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;

public final class BigCannonIntegration {
    private BigCannonIntegration() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, BigCannonEventHandler::onProjectileDamage);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, BigCannonEventHandler::onEntityJoinLevel);
    }
}
