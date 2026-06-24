package dev.customclaims.protection.event;

import dev.customclaims.protection.CustomClaimsProtectionMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class BlockInteractionHandler {
    private BlockInteractionHandler() {
    }

    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        boolean allowedByLimits = CustomClaimsProtectionMod.services()
                .foreignInteractionLimitService()
                .wouldAllowBreak(player, level, event.getPos());
        boolean allowedByStorage = CustomClaimsProtectionMod.services()
                .storageProtectionService()
                .canBreak(player, level, event.getPos(), level.getBlockState(event.getPos()));

        if (!allowedByLimits || !allowedByStorage) {
            event.setCanceled(true);
            return;
        }

        grantOpenPartiesBypassIfNeeded(player, level, event.getPos());
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (CustomClaimsProtectionMod.services()
                .foreignInteractionLimitService()
                .wouldAllowPlace(player, level, event.getPos())) {
            grantOpenPartiesBypassIfNeeded(player, level, event.getPos());
        }
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
            return;
        }

        grantOpenPartiesBypassIfNeeded(player, level, event.getPos());
    }

    public static void onPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        boolean allowedByLimits = CustomClaimsProtectionMod.services()
                .foreignInteractionLimitService()
                .canPlace(player, level, event.getPos());
        if (!allowedByLimits) {
            event.setCanceled(true);
            return;
        }

        grantOpenPartiesBypassIfNeeded(player, level, event.getPos());
    }

    private static void grantOpenPartiesBypassIfNeeded(ServerPlayer player, ServerLevel level, net.minecraft.core.BlockPos pos) {
        if (CustomClaimsProtectionMod.services()
                .foreignInteractionLimitService()
                .shouldBypassOpenPartiesProtection(player, level, pos)) {
            CustomClaimsProtectionMod.services()
                    .openPartiesProtectionBypassService()
                    .grantUntilNextServerTick(level, player);
        }
    }
}
