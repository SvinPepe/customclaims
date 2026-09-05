package dev.customclaims.protection.event;

import dev.customclaims.protection.CustomClaimsProtectionMod;
import dev.customclaims.protection.ProtectionServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class EntityInteractionHandler {
    private EntityInteractionHandler() {
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!event.isCanceled() && event.getEntity() instanceof ServerPlayer player) {
            grantBypassIfNeeded(player, event.getTarget());
        }
    }

    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!event.isCanceled() && event.getEntity() instanceof ServerPlayer player) {
            grantBypassIfNeeded(player, event.getTarget());
        }
    }

    private static void grantBypassIfNeeded(ServerPlayer player, Entity target) {
        ProtectionServices services = CustomClaimsProtectionMod.services();
        if (services.entityInteractionProtectionService().shouldBypassOpenPartiesProtection(player, target)) {
            services.openPartiesProtectionBypassService().grantUntilNextServerTick(player.serverLevel(), player);
        }
    }
}
