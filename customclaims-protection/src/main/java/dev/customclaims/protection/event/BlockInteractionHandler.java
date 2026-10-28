package dev.customclaims.protection.event;

import dev.customclaims.core.CustomClaimsCoreMod;
import dev.customclaims.core.api.model.TerritoryStatus;
import dev.customclaims.protection.CustomClaimsProtectionMod;
import dev.customclaims.protection.compat.WarfareBridgeHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

public final class BlockInteractionHandler {
    private BlockInteractionHandler() {
    }

    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        BlockPos pos = event.getPos();
        if (isFakePlayer(player)) {
            if (handleFakePlayerInteraction(player, level, pos)) {
                event.setCanceled(true);
            }
            return;
        }

        boolean allowedByLimits = CustomClaimsProtectionMod.services()
                .foreignInteractionLimitService()
                .wouldAllowBreak(player, level, pos);

        if (!allowedByLimits) {
            event.setCanceled(true);
            return;
        }

        grantOpenPartiesBypassIfNeeded(player, level, pos);
    }

    public static void onBreakBlock(BreakBlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        BlockPos pos = event.getPos();
        if (isFakePlayer(player)) {
            if (handleFakePlayerInteraction(player, level, pos)) {
                event.setCanceled(true);
            }
            return;
        }

        boolean allowedByLimits = CustomClaimsProtectionMod.services()
                .foreignInteractionLimitService()
                .canBreak(player, level, pos);

        if (!allowedByLimits) {
            event.setCanceled(true);
            return;
        }

        grantOpenPartiesBypassIfNeeded(player, level, pos);
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        BlockPos pos = event.getPos();
        if (isFakePlayer(player)) {
            if (handleFakePlayerInteraction(player, level, pos)) {
                event.setCanceled(true);
            }
            return;
        }

        if (CustomClaimsProtectionMod.services()
                .foreignInteractionLimitService()
                .wouldAllowPlace(player, level, pos)) {
            grantOpenPartiesBypassIfNeeded(player, level, pos);
        }
    }

    public static void onPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        BlockPos pos = event.getPos();
        if (isFakePlayer(player)) {
            if (handleFakePlayerInteraction(player, level, pos)) {
                event.setCanceled(true);
            }
            return;
        }

        boolean allowedByLimits = CustomClaimsProtectionMod.services()
                .foreignInteractionLimitService()
                .canPlace(player, level, pos);
        if (!allowedByLimits) {
            event.setCanceled(true);
            return;
        }

        grantOpenPartiesBypassIfNeeded(player, level, pos);
    }

    /**
     * Handles fake player / Deployer interaction against claim ownership.
     * TODO: evaluate owner via BlockEntity / placement in future work to distinguish
     * foreign cross-border machines from friendly internal automation.
     * Currently grants bypass to prevent breaking automated farms inside player claims.
     */
    private static boolean handleFakePlayerInteraction(ServerPlayer player, ServerLevel level, BlockPos pos) {
        ChunkPos chunkPos = ChunkPos.containing(pos);
        if (WarfareBridgeHolder.isContestedWarChunk(level, chunkPos)) {
            return false;
        }

        TerritoryStatus status = CustomClaimsCoreMod.services().territoryService().getStatus(level, chunkPos);
        if (status == TerritoryStatus.UNCLAIMED || status == TerritoryStatus.WAR_CONTESTED) {
            return false;
        }

        CustomClaimsProtectionMod.services()
                .openPartiesProtectionBypassService()
                .grantUntilNextServerTick(level, player);
        return false;
    }

    private static boolean isFakePlayer(ServerPlayer player) {
        return player instanceof FakePlayer || player.getClass().getName().contains("FakePlayer");
    }

    private static void grantOpenPartiesBypassIfNeeded(ServerPlayer player, ServerLevel level, BlockPos pos) {
        if (CustomClaimsProtectionMod.services()
                .foreignInteractionLimitService()
                .shouldBypassOpenPartiesProtection(player, level, pos)) {
            CustomClaimsProtectionMod.services()
                    .openPartiesProtectionBypassService()
                    .grantUntilNextServerTick(level, player);
        }
    }
}
