package dev.customclaims.protection.event;

import dev.customclaims.core.CustomClaimsCoreMod;
import dev.customclaims.core.api.model.ClaimSideId;
import dev.customclaims.core.api.model.TerritoryStatus;
import dev.customclaims.protection.CustomClaimsProtectionMod;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

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

    public static void onBreakBlock(BlockEvent.BreakEvent event) {
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
     * Checks fake player / Deployer interaction against claim ownership.
     * Returns true if interaction should be BLOCKED (cross-border tampering).
     * Returns false if interaction is allowed (wild, contested war, or owned by claim member).
     */
    private static boolean handleFakePlayerInteraction(ServerPlayer player, ServerLevel level, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        TerritoryStatus status = CustomClaimsCoreMod.services().territoryService().getStatus(level, chunkPos);
        if (status == TerritoryStatus.UNCLAIMED || status == TerritoryStatus.WAR_CONTESTED) {
            return false;
        }

        Optional<ClaimSideId> owner = CustomClaimsCoreMod.services().territoryService().getClaimOwnerSide(level, chunkPos);
        if (owner.isEmpty()) {
            return false;
        }

        boolean isMember = isOwnerOrMember(level.getServer(), owner.get(), player.getUUID());
        if (isMember) {
            // Claim owner / member Deployer inside friendly claim: grant bypass so auto-farms run smoothly
            CustomClaimsProtectionMod.services()
                    .openPartiesProtectionBypassService()
                    .grantUntilNextServerTick(level, player);
            return false;
        }

        // Outsider machine attempting cross-border tampering in peaceful claim: block!
        return true;
    }

    private static boolean isFakePlayer(ServerPlayer player) {
        return player instanceof FakePlayer || player.getClass().getName().contains("FakePlayer");
    }

    private static boolean isOwnerOrMember(MinecraftServer server, ClaimSideId sideId, UUID actorId) {
        if (actorId == null) {
            return false;
        }
        if (sideId.isPlayer()) {
            return sideId.playerUuid().filter(actorId::equals).isPresent();
        }
        return CustomClaimsCoreMod.services().partyService().sideMemberIds(server, sideId).contains(actorId);
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
