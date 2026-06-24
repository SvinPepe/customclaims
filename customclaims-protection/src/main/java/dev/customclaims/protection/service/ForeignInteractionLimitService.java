package dev.customclaims.protection.service;

import dev.customclaims.core.api.model.TerritoryStatus;
import dev.customclaims.core.permissions.CustomClaimsPermissions;
import dev.customclaims.core.service.PermissionService;
import dev.customclaims.core.service.TerritoryService;
import dev.customclaims.protection.config.ProtectionConfig;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public final class ForeignInteractionLimitService {
    private final TerritoryService territoryService;
    private final PermissionService permissionService;
    private final Map<UUID, Integer> foreignBreaks = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> foreignPlacements = new ConcurrentHashMap<>();
    private volatile Instant nextResetAt;

    public ForeignInteractionLimitService(TerritoryService territoryService, PermissionService permissionService) {
        this.territoryService = territoryService;
        this.permissionService = permissionService;
    }

    public boolean canBreak(ServerPlayer player, ServerLevel level, BlockPos pos) {
        tickResetIfDue(Instant.now());
        if (permissionService.hasPermission(player, CustomClaimsPermissions.BYPASS)) {
            return true;
        }

        TerritoryStatus status = territoryService.getInteractionStatus(player, level, new ChunkPos(pos));
        if (status == TerritoryStatus.WAR_CONTESTED || status == TerritoryStatus.UNCLAIMED) {
            return true;
        }
        if (status != TerritoryStatus.FOREIGN_LIMITED_INTERACTION) {
            return true;
        }

        return incrementIfBelowLimit(foreignBreaks, player.getUUID(), ProtectionConfig.FOREIGN_BLOCK_BREAK_LIMIT.get());
    }

    public boolean wouldAllowBreak(ServerPlayer player, ServerLevel level, BlockPos pos) {
        tickResetIfDue(Instant.now());
        if (permissionService.hasPermission(player, CustomClaimsPermissions.BYPASS)) {
            return true;
        }

        TerritoryStatus status = territoryService.getInteractionStatus(player, level, new ChunkPos(pos));
        if (status == TerritoryStatus.WAR_CONTESTED || status == TerritoryStatus.UNCLAIMED) {
            return true;
        }
        if (status != TerritoryStatus.FOREIGN_LIMITED_INTERACTION) {
            return true;
        }

        return hasRemaining(foreignBreaks, player.getUUID(), ProtectionConfig.FOREIGN_BLOCK_BREAK_LIMIT.get());
    }

    public boolean canPlace(ServerPlayer player, ServerLevel level, BlockPos pos) {
        tickResetIfDue(Instant.now());
        if (permissionService.hasPermission(player, CustomClaimsPermissions.BYPASS)) {
            return true;
        }

        TerritoryStatus status = territoryService.getInteractionStatus(player, level, new ChunkPos(pos));
        if (status == TerritoryStatus.WAR_CONTESTED || status == TerritoryStatus.UNCLAIMED) {
            return true;
        }
        if (status != TerritoryStatus.FOREIGN_LIMITED_INTERACTION) {
            return true;
        }

        return incrementIfBelowLimit(foreignPlacements, player.getUUID(), ProtectionConfig.FOREIGN_BLOCK_PLACE_LIMIT.get());
    }

    public boolean wouldAllowPlace(ServerPlayer player, ServerLevel level, BlockPos pos) {
        tickResetIfDue(Instant.now());
        if (permissionService.hasPermission(player, CustomClaimsPermissions.BYPASS)) {
            return true;
        }

        TerritoryStatus status = territoryService.getInteractionStatus(player, level, new ChunkPos(pos));
        if (status == TerritoryStatus.WAR_CONTESTED || status == TerritoryStatus.UNCLAIMED) {
            return true;
        }
        if (status != TerritoryStatus.FOREIGN_LIMITED_INTERACTION) {
            return true;
        }

        return hasRemaining(foreignPlacements, player.getUUID(), ProtectionConfig.FOREIGN_BLOCK_PLACE_LIMIT.get());
    }

    public String limitsStatus(ServerPlayer player) {
        Instant now = Instant.now();
        tickResetIfDue(now);
        int breakLimit = ProtectionConfig.FOREIGN_BLOCK_BREAK_LIMIT.get();
        int placeLimit = ProtectionConfig.FOREIGN_BLOCK_PLACE_LIMIT.get();
        int breaks = foreignBreaks.getOrDefault(player.getUUID(), 0);
        int places = foreignPlacements.getOrDefault(player.getUUID(), 0);
        long resetSeconds = Math.max(0L, Duration.between(now, nextResetAt).getSeconds());
        return "Foreign claim limits: breaks " + breaks + "/" + breakLimit
                + ", placements " + places + "/" + placeLimit
                + ", reset in " + formatDuration(resetSeconds) + ".";
    }

    public boolean shouldBypassOpenPartiesProtection(ServerPlayer player, ServerLevel level, BlockPos pos) {
        if (permissionService.hasPermission(player, CustomClaimsPermissions.BYPASS)) {
            return true;
        }

        TerritoryStatus status = territoryService.getInteractionStatus(player, level, new ChunkPos(pos));
        return status == TerritoryStatus.WAR_CONTESTED
                || status == TerritoryStatus.FOREIGN_LIMITED_INTERACTION;
    }

    public synchronized void tickResetIfDue(Instant now) {
        ensureResetScheduled(now);
        if (now.isBefore(nextResetAt)) {
            return;
        }
        resetAll(now);
    }

    public synchronized void rescheduleReset(Instant now) {
        nextResetAt = nextResetAfter(now);
    }

    public synchronized void resetAll() {
        resetAll(Instant.now());
    }

    public synchronized void reset(UUID playerId) {
        foreignBreaks.remove(playerId);
        foreignPlacements.remove(playerId);
    }

    private void resetAll(Instant now) {
        foreignBreaks.clear();
        foreignPlacements.clear();
        nextResetAt = nextResetAfter(now);
    }

    private boolean incrementIfBelowLimit(Map<UUID, Integer> usage, UUID playerId, int limit) {
        if (!hasRemaining(usage, playerId, limit)) {
            return false;
        }
        usage.put(playerId, usage.getOrDefault(playerId, 0) + 1);
        return true;
    }

    private boolean hasRemaining(Map<UUID, Integer> usage, UUID playerId, int limit) {
        if (limit <= 0) {
            return false;
        }
        int current = usage.getOrDefault(playerId, 0);
        return current < limit;
    }

    private Instant nextResetAfter(Instant now) {
        return now.plusSeconds(ProtectionConfig.FOREIGN_INTERACTION_LIMIT_RESET_INTERVAL_SECONDS.get());
    }

    private void ensureResetScheduled(Instant now) {
        if (nextResetAt == null) {
            nextResetAt = nextResetAfter(now);
        }
    }

    private String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (hours > 0) {
            return hours + "h " + minutes + "m " + seconds + "s";
        }
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }
}
