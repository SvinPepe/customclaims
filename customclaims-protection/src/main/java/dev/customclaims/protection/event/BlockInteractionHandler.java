package dev.customclaims.protection.event;

import dev.customclaims.protection.CustomClaimsProtectionMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class BlockInteractionHandler {
    private BlockInteractionHandler() {
    }

    public static void onBreakBlock(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        boolean allowedByLimits = CustomClaimsProtectionMod.services()
                .foreignInteractionLimitService()
                .canBreak(player, level, event.getPos());
        boolean allowedByStorage = CustomClaimsProtectionMod.services()
                .storageProtectionService()
                .canBreak(player, level, event.getPos(), event.getState());

        if (!allowedByLimits || !allowedByStorage) {
            event.setCanceled(true);
        }
    }

    public static void onPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!CustomClaimsProtectionMod.services().foreignInteractionLimitService().canPlace(player, level, event.getPos())) {
            event.setCanceled(true);
        }
    }
}
