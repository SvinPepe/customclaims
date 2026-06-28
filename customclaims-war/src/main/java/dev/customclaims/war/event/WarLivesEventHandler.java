package dev.customclaims.war.event;

import dev.customclaims.war.CustomClaimsWarMod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public final class WarLivesEventHandler {
    private WarLivesEventHandler() {
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CustomClaimsWarMod.services().warManager().onPlayerDeath(player);
        }
    }
}
