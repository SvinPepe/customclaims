package dev.customclaims.war.event;

import dev.customclaims.war.CustomClaimsWarMod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class WarActivityHandler {
    private WarActivityHandler() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CustomClaimsWarMod.services().afkTracker().markActive(player);
        }
    }

    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CustomClaimsWarMod.services().afkTracker().markActive(player);
        }
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CustomClaimsWarMod.services().afkTracker().markActive(player);
        }
    }
}
